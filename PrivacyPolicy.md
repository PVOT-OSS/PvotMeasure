<!--
SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: GPL-3.0-or-later
-->

# Privacy Policy — Measure

_Effective 31 October 2026_

Measure (package `org.prauga.measure`) is a measuring tape and spirit level app published by
Saalim Quadri for PVOT-OSS. This policy explains what the app does with your data.

**In short: the app doesn't collect, send, share or sell any personal data. It has no accounts, no
ads, no analytics and no tracking. Camera images are used live on your device and never saved.**

## What the app uses

- **The camera**, only in the Measure view and only if you allow it, so you can place points in
  the room and measure the distance between them. The camera feed is processed live on your device
  and is never recorded, saved or sent anywhere by the app. Level and Settings work without it.
- **The motion sensors** (accelerometer and gyroscope), to show how far your device is tilted in
  the Level view and, together with the camera, to track your device's movement while measuring.
  Readings are used live and never stored.

## Google Play Services for AR

The Measure view runs on Google Play Services for AR (ARCore), which is provided by Google LLC and
governed by the [Google Privacy Policy](https://policies.google.com/privacy). It is a separate app
on your device; if it isn't installed, Android may offer to install it from Google Play when you
open the Measure view.

## What the app keeps on your device

- **Your settings**: whether liquid glass is on and whether lengths are shown in imperial units.

Measurements are not saved: they disappear when you clear them or close the app. The settings
stay in the app's private storage and are deleted when you clear the app's storage or uninstall
it.

## Backups

If you turn on Android's device backup, Android may include the app's settings in your device
backup (for example, Google's backup on most devices), under the terms of that backup service. The
app itself never sends them anywhere.

## Permissions

- `CAMERA`: to see the room in the Measure view, as described above. The app asks for it when you
  open the Measure view; you can refuse.
- `VIBRATE`: for a short tap when the level reaches flat or a point is placed.
- `INTERNET`: added by the SceneView library the Measure view is built on. The app itself makes no
  network requests and sends nothing; Google Play Services for AR may use the network under its own
  terms, as described above.

## Children

The app is not directed at children and collects no data from anyone, children included.

## Changes

If the app starts handling data differently, this policy will be updated before that version is
released, with a new effective date at the top.

## Contact

Questions about this policy: danascape@gmail.com, or open an issue at
https://github.com/PVOT-OSS/PvotMeasure/issues.
