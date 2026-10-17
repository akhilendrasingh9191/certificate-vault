const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("Unauthenticated user: cannot read certificates", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("certificates").get());
});

test("Authenticated user: can create valid certificate in their own subcollection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("certificates").doc("cert_1").set({
      userId: ALICE_UID,
      title: "Google Cloud Certified Associate Cloud Engineer",
      organization: "Google Cloud",
      issueDate: "2024-03-15",
      category: "Certification",
      description: "Demonstrated ability to deploy applications and manage enterprise solutions on GCP.",
      skills: ["Google Cloud", "Kubernetes", "IAM"],
    })
  );
});

test("Authenticated user: cannot read another user's certificate", async () => {
  // Seed data as admin
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("certificates").doc("alice_cert").set({
      userId: ALICE_UID,
      title: "Secret Certification",
      organization: "Top Secret Org",
      issueDate: "2024-01-01",
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("certificates").doc("alice_cert").get());
});

test("Authenticated user: cannot create certificate in another user's subcollection", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("certificates").doc("spoofed_cert").set({
      userId: BOB_UID,
      title: "Hacked Cert",
      organization: "Attacker Org",
      issueDate: "2024-01-01",
    })
  );
});
