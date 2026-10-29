<!--
SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: GPL-3.0-or-later
-->

# Privacy Policy — Measure

_Effective 30 October 2026_

Measure (package `org.prauga.measure`) is a measuring and spirit level app published by Saalim
Quadri for PVOT-OSS. This policy explains what the app does with your data.

**In short: the app doesn't collect, store, send, share or sell any personal data. It has no
accounts, no ads, no analytics and no tracking, and it has no permission to use the internet.**

## What the app uses

- **The accelerometer**, to show how far your device is tilted in the Level view. Readings are used
  live while the Level view is open and are never stored.

## What the app keeps on your device

Nothing. The app saves no files or settings; closing it discards everything it showed.

## What other services see

None. The app sends nothing anywhere and relies on no online service.

## Permissions

The app asks for no permissions. Its manifest contains only
`org.prauga.measure.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, which Android's AndroidX library adds
so that the app's own components can talk to each other; it gives the app access to none of your
data. The app does not have the `INTERNET` permission.

## Children

The app is not directed at children and collects no data from anyone, children included.

## Changes

If the app starts handling data differently, this policy will be updated before that version is
released, with a new effective date at the top.

## Contact

Questions about this policy: danascape@gmail.com, or open an issue at
https://github.com/PVOT-OSS/PvotMeasure/issues.
