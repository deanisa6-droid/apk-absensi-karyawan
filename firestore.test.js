const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfilePayload(uid, timestamp = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    email: "alice@example.com",
    fullName: "Alice Pratama",
    username: "alice.pratama",
    phone: "081234567890",
    birthDate: "14/08/1998",
    gender: "Perempuan",
    city: "Jakarta",
    occupation: "Product Designer",
    bio: "Penggemar antarmuka glassmorphism modern.",
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

test("Unauthenticated user: cannot read or write profiles or activities", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("activities").get()
  );
});

test("Authenticated owner: valid Kotlin create, partial update, and delete succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update mirroring UserProfileRepository.updateProfile()
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      fullName: "Alice Pratama Wijaya",
      city: "Bandung",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field / @DocumentId 'id' leakage
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      city: "Surabaya",
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject invalid username / gender bounds
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      username: "ab",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      gender: "Unknown",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).delete());
});

test("Cross-user isolation: Alice cannot read, create, or modify Bob's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .set(validProfilePayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      fullName: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set(validProfilePayload(ALICE_UID))
  );
});

test("Activities subcollection: owner can create and query ordered list; updates and cross-user access fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const actRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("activities")
    .doc("act_1");

  await assertSucceeds(
    actRef.set({
      userId: ALICE_UID,
      action: "REGISTER",
      title: "Registrasi Berhasil",
      description: "Data diri berhasil didaftarkan ke LuminaID.",
      createdAt: new Date(Date.now() - 2000),
    })
  );

  // Mirrors Kotlin repository query: orderBy("createdAt", DESCENDING).limit(20)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("activities")
      .orderBy("createdAt", "desc")
      .limit(20)
      .get()
  );

  // Activities are immutable
  await assertFails(actRef.update({ title: "Tampered" }));

  // Bob cannot list or write Alice's activities
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("activities").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("activities")
      .doc("act_bob")
      .set({
        userId: BOB_UID,
        action: "LOGIN",
        title: "Spoof",
        description: "Spoof attempt",
        createdAt: new Date(Date.now() - 1000),
      })
  );
});
