'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


/*
 * 'OnWrite' works as 'addValueEventListener' for android. It will fire the function
 * everytime there is some item added, removed or changed from the provided 'database.ref'
 * 'sendNotification' is the name of the function, which can be changed according to
 * your requirement
 */

exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}')
                                .onWrite((data, context) => {


  /*
   * You can store values as variables from the 'database.ref'
   * Just like here, I've done for 'user_id' and 'notification'
   */

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a notification from : ', user_id);

  const fromUser = admin.database().ref(`/Notifications/${user_id}/${notification_id}`).once('value');
  return fromUser.then(fromUserResult => {
    const from_user_id = fromUserResult.val().from;
    console.log('You have a new notification from : ', from_user_id);
    const userQuery = admin.database().ref(`/Users/${from_user_id}/name`).once('value');
    return userQuery.then(userResult => {

      const userName = userResult.val();

      const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');
      return deviceToken.then(result => {
        const token_id = result.val();
        const payload = {
          notification: {
            title: "Friend Request",
            body: `${userName} has sent you a friend request`,
            icon: "default",
            click_action : "myapp.training.jason.com.firstchatappjason_TARGET_NOTIFICATION"
          },
          data : {
            from_user_id : from_user_id
          }
          };
          return admin.messaging().sendToDevice(token_id, payload).then(response => {
          console.log('This was notification feature');
          return true;
          });
        });


    });



  });
  });
