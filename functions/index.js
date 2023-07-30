const functions = require("firebase-functions");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");

const ALGOLIA_APP_ID = "EWKHGBBMT7";
const ALGOLIA_ADMIN_KEY = "79724b305aa483e93838c4b2fb978150";
const ALGOLIA_INDEX_NAME = "dev_pikul";

admin.initializeApp(functions.config().firebase);

exports.createObject = functions.firestore
    .document("businesses/{businessId}")
    .onCreate(async (snap, context) => {
      const newValue = snap.data();
      newValue.objectID = snap.id;
      const client = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_KEY);

      const index = client.initIndex(ALGOLIA_INDEX_NAME);
      index.saveObject(newValue);
      console.log("Finished");
    });

exports.updateObject = functions.firestore
    .document("businesses/{businessId}")
    .onCreate(async (snap, context) => {
      const afterUpdate = snap.after.data();
      afterUpdate.objectID = snap.after.id;
      const client = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_KEY);

      const index = client.initIndex(ALGOLIA_INDEX_NAME);
      index.saveObject(afterUpdate);
      console.log("Finished");
    });

exports.deleteObject = functions.firestore
    .document("businesses/{businessId}")
    .onDelete(async (snap, context) => {
      const oldID = snap.id;
      const client = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_KEY);

      const index = client.initIndex(ALGOLIA_INDEX_NAME);
      index.deleteObject(oldID);
    });
