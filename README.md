# Team nimbus presents

AR•CNE is an Augmented Reality (AR) based application that mainly helps people organizing groups from anywhere. Using a combination of AR and GPS, people can use this app through their smartphone's camera to locate and navigate to their friends' or colleagues' location. This app also has group and chat system that allows people to chat together and share their locations. (Currently on Android only)

## List of the app features:

- Augmented reality route tracking
- Group with chat system
- Private user to user chat
- Sharing location in group or private chat
- User sign-up/sign-in with customizable profile page

## Device Requirements

- Android device running SDK>=23
- Android version 6.0 or above

## Download and Use

To download raw project, simply Clone this rep and open folder using a Java based IDE (preferably Android Studio).

To try this app in Android Studio, Run the app using an Android smartphone or an emulator (which can be made in through Android Studio).

Note: This app requires constant Internet connection and GPS tracking to work properly, so using an emulator means you might need to mock your location

### Using Map and Routing

- sign up or login
- go to map tab
- select and pick a group that you want to track
- pick a user marker that you want as a destination target and click it
- click "select as destination" text at the top of the marker you clicked
- once the destination and routing is rendered, you can switch to AR mode by togling the AR switch at the 
  top right of the map
  
### Using AR Feature
- after switching to AR mode, the user will be directed to the AR View
- in the AR View you will see 2 compasses, one showing the device's direction in relation to the four cardinal
directions and the other one showing the device's direction in relation to target point/location
- in the AR View you can choose 2 types of the AR Mode by toogling the Mode Switch on the top right corner
- the first mode is Routing Mode, where we split the polyline in from the map to a list of checkpoints. The next checkpoint in the route will be rendered as a red dot, and will change to the next one as we move near the current checkpoint. The user can also see how many checkpoints left until the destination.
- the second mode is Destination Target Mode, where we directly set the red dot on the AR View as the direction and location of the target location.
- in the AR View, the user can see the distance between the user and the target checkpoint/location.
- to go back to the map feature, the user simply need to press the back button on their smartphone

## Members of nimbus:

Name | Email | Student ID
---- | ----- | ----------
Arnold Angelo | aangelo@student.unimelb.edu.au | 783859
Christopher Hadi Oetomo | coetomo@student.unimelb.edu.au | 755374
Leonardus Elbert Putra | lputra@student.unimelb.edu.au | 755379
Nico Dinata | ndinata@student.unimelb.edu.au | 770318
Richard Aldrich | rsiem@student.unimelb.edu.au | 716039

## Acknowledgement
- drawing route between 2 points: https://www.journaldev.com/13373/android-google-map-drawing-route-two-points by ANUPAM CHUGH
- inspired by TVAC Studio
- AR inspiration and source : https://github.com/dat-ng/ar-location-based-android
- compass base : https://www.youtube.com/watch?v=RcqXFxqIAW4
