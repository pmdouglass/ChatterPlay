const functions = require("firebase-functions/v2");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// 🔹 Function to update Firestore with Alert Type
async function updateJavaSystemAlertType(crRoomId, alertType) {
    try {
        const roomRef = db.collection("ChatriseRooms").doc(crRoomId);
        await roomRef.set({ AlertType: alertType }, { merge: true });

        console.log(`✅ AlertType updated successfully: ${alertType}`);
        return true;
    } catch (error) {
        console.error(`❌ Error updating AlertType: ${error.message}`);
        return false;
    }
}

// 🔹 Scheduled Function: Runs every 24 hours
const checkAndRunAlert = onSchedule("every 24 hours", async (event) => {
    console.log("🔥 CheckAndRun: Function Running...");

    const now = new Date();
    console.log(`✅ Current Timestamp: ${now}`);

    const roomsRef = db.collection("ChatriseRooms");
    const snapshot = await roomsRef.get();

    console.log(`📌 Found ${snapshot.size} rooms to check.`);

    for (const doc of snapshot.docs) {
        console.log(`🔍 Checking Room ID: ${doc.id}`);

        const data = doc.data();
        if (!data.createdAt) {
            console.log(`⚠️ Room ${doc.id} is missing 'createdAt' field.`);
            continue;
        }

        const createdAt = data.createdAt.toDate();
        console.log(`📅 Room Created At: ${createdAt}`);

        const diffMillis = now.getTime() - createdAt.getTime();
        const totalDaysElapsed = Math.floor(diffMillis / (1000 * 60 * 60 * 24)) + 1;
        console.log(`⏳ Total Days Elapsed: ${totalDaysElapsed}`);

        const ALERT_SCHEDULE = {
            1: "game",
            2: "game_results",
            3: "ranking",
            4: "rank_results",
            5: "top_discuss",
            6: "blocking",
            7: "last_message",
            8: "new_player",
            9: "none",
        };

        if (ALERT_SCHEDULE[totalDaysElapsed]) {
            const alertType = ALERT_SCHEDULE[totalDaysElapsed];

            console.log(`🚨 Triggering Alert Type: ${alertType} for Room ${doc.id}`);

            try {
                await updateJavaSystemAlertType(doc.id, alertType);
                console.log(`✅ Alert Triggered: ${alertType} for Room ${doc.id}`);
            } catch (error) {
                console.error(`❌ updateSystemAlertType Failed for Room ${doc.id}:`, error.message);
            }
        } else {
            console.log(`🟡 No alert scheduled for Day ${totalDaysElapsed} in Room ${doc.id}`);
        }
    }

    console.log("✅ CheckAndRun: Alert check completed.");
});

// 🔹 ✅ **Fixed Export**
module.exports = { updateJavaSystemAlertType, checkAndRunAlert };
