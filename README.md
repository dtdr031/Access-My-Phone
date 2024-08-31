#Access My Phone
Project Overview: "Access My Phone" is an Android application designed to remotely manage and control a mobile device via SMS, without requiring an active internet connection. This project focuses on enhancing the security and accessibility of smartphones, particularly in scenarios where the device is not in the user's vicinity.

#Features:

1. Device Identification: Retrieve the unique device ID remotely.
2. Contact Management: Access and retrieve contact details from the host phone.
3. Location Tracking: Obtain the current GPS coordinates of the host phone.
4. Remote PIN Management: Generate and update a random PIN remotely.
5. Mode Control: Switch between ring and vibrate modes of the device.
6. Screen Lock: Lock the device screen remotely for added security.
7. No Internet Required: Operates entirely through SMS, ensuring functionality even without internet access.

#Technical Specifications:
1.Platform: Android
2.Development Environment: Android Studio
3.Programming Language: Java 8
4.Supported Android Versions: Android 8.0 (Oreo) and above
5.Tools Used: Lucidchart, SmartDraw, Microsoft Whiteboard

#System Requirements:
1. The application must be installed on the host phone (the device you want to control).
2. The controlling device does not require the app installation; all operations are performed via SMS.
3. Minimum Android version required: 8.0 (Oreo).

#Usage:

1.Setup: Install the application on the host phone and configure a secure PIN.
2.Service Activation: Start the service on the host phone.
3.Remote Commands: Use another phone to send SMS commands to the host phone:
4.Retrieve Device ID: <PIN> get id
5.Retrieve Contacts: <PIN> contact <contact_name>
6.Lock Screen: <PIN> lock
7.Get Location: <PIN> location
8.Change Mode to Vibrate: <PIN> vibrate
9.Change Mode to Ring: <PIN> ring
10.Generate New PIN: <PIN> pass

#Future Enhancements:
1.Integration with smartwatches, tablets, and other devices.
2.Timed GPS location updates.
3.Additional security features like fingerprint authentication.
4.Enhanced user interface for easier management of commands and settings.

#Contributors:
1. Dattatraya Deb
2. Rohit Thaosen
3. Tareni Nayak
4. Govind Yadav
