# HotelBooker (Java Swing) – Guest + Admin Hotel Booking System

This is a Java Swing hotel booking system with:

- **Guest portal**: browse rooms, search by destination, open room details, **book a room**.
- **Guest accounts**: **Register / Login**, **My Profile**.
- **Guest portal**: **My Bookings** (view your bookings, cancel confirmed booking, export receipt).
- **Receipts**: auto-generates a **PDF receipt** in `receipts/`.
- **Admin portal**: dashboard + **Manage Rooms (CRUD + image picker)** + **Bookings list (cancel + export receipt)** + **Guests list**.
- **Pictures**: room cards and room details show real images from `assets/images/`.
- **File-based data** (no database needed):
  - `data/master/rooms.txt`
  - `data/users.txt`
  - `data/bookings.txt`
  - `data/payments.txt`
  - `receipts/`

## Requirements

- Java **JDK 17+** (JDK 11 can work too, but 17 is recommended)

## Run (VS Code)

1. Open the folder `hotelbooker/` in VS Code.
2. Open a terminal inside VS Code.
3. Run:

### Windows
```bat
run.bat
```

### macOS / Linux
```bash
chmod +x run.sh
./run.sh
```

## Admin Login

Click **Admin Login** in the top navbar.

The admin credentials are defined in `src/admin/service/AdminAuth.java`.

## Guest: My Bookings

Click **My Bookings** in the top navbar.

- Search by guest name
- See booking list
- Cancel a confirmed booking (room becomes available again)
- Export receipt PDF for confirmed bookings

## Guest Accounts

Use the navbar buttons:

- **Register** → creates account in `data/users.txt`
- **Login** → required before booking / viewing bookings
- **My Profile** → change name and optionally password

## Room Images

Room images are stored in:

`assets/images/`

And referenced from:

`data/master/rooms.txt`

Schema (current):

```
id|hotel|location|price|rating|reviews|amenities|capacity|available|imagePath
```

Example:

```
R1|Grand Plaza Hotel|New York, USA|299|4.8|324|Free WiFi,Pool,Spa|2|true|assets/images/grand_plaza.jpg
```

## Theme (NEW)

Use the **Theme** button in the top navbar to switch between **Light** and **Dark** mode.

## Optional: Build a single runnable JAR (NEW)

### Windows
```bat
build_jar.bat
```

### macOS / Linux
```bash
chmod +x build_jar.sh
./build_jar.sh
```

Then run:
```bash
java -jar dist/HotelBooker.jar
```

## V14 Add-ons
- Admin/Staff accounts (data/staff.txt)
  - admin / 0000 (ADMIN)
  - staff / 1234 (STAFF)
- Admin Promo Codes manager (CRUD)
  - Manage in Admin → Promo Codes
  - Stored in data/promocodes.txt

