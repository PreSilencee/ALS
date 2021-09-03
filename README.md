# A Little Share (ALS) (Final Year Project)
# Introduction
- an android based mobile application that provide a charity platform to the user.
- allows the user to represent the contributor or organization to raise the charity event

# Software that Implementation ALS
- Android Studio (IDE)
- JAVA
- Firebase (Database)

# Functional description and specifications
1. Register Function
   - Users can register an account as Contributor or Organization by using their own 
     email. The email should be real and active in order to receive the email verification 
     after created the account.
     
2. Send Email Verification Function
   - Users can receive the email from the server when they register using email and 
     password. This function able to verify the user’s email to prevent the user use the 
     “ghost” email to register an account.

3. Login Function
   - Users can login into the mobile application after created an account. However, users 
     must verify the email before login. The mobile application will not allow users to 
     login if the email have not verified.
 
4. Forgot Password Function
   - This function is allowed the users to change the password when they forgot 
     password for the login sections. Users can enter their own email that have been 
     registered into this mobile application and send the request for changing password.
     
5. Change Password Function
   - This function allows the users to change the password after they login into the 
     mobile application. Users can enter old password, new password and confirm 
     password to change the password for the account.
     
6. First-Time Set Up Function
   - This function is allowed the users to set up their profile image when they are first-time login. Users who register as contributor can skip this section without set up
     the profile image. However, users who register as organization must finish the set 
     up.
     
7. Account Modify Function
   - This function is allowed the users to change their personal profile image and 
     personal details excluded the email. However, if the users are the organization, they 
     would allow to change the profile image only.
     
8. Create Event Function
   - This function is allowed the users create the fund-raising event. Users need to enter 
     title, descriptions, start date, end date and target amount that want to raise.
     
9. Donate Function
   - This function is allowed the users to donate the amount that want to donate for the 
     event. It allows user uses the card payment to donate the amount.
     
10. Message Chat Function
    - This function is allowed the users to chat among themselves. User can communicate 
      to the other users by sending the message in the mobile application. User also can 
      receive the notification if there have other users sent message to him/her. For the 
      notification, it must connect to the firebase server first then only allowed user to 
      send notification to other users.
11. View / Save donation receipt Function
    - This Function allows the users to view the donation receipt and save it as pdf. For 
      example, the mobile application will download the donation receipt into the local 
      storage of the user’s phone.

12. Search Function
    - This function allows the users to search the event, contributor, and organization 
      by using the keywords

# Implementation
- Firebase Email/Password Authentication
- Google Authentication
- Facebook Authentication
- Firebase Realtime Database
- Firebase Storage
- Firebase Cloud Messaging
- RazorPay
- End-to-end encryption
