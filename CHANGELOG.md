# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.8.1] - 2018-09-04
### Changed
- Moved code from mainListFragment to MainActivity to ensure that data persists among different fragments.
This version is stated for Beta Release.

## [0.8.0] - 2018-08-18
### Added
- Added Fragments for more modular UI
- Added an About section
- Added ability to sign out
- Added an upgrade option (not implemented)
- Added share option (not implemented)
- Now you can see your Google account profile picture and name with which you are logged in.
### Fixed
- Fixed a lot of bugs (and added some more to fix next time!)  
See Pull request #6 for more

## [0.7.0] 2018-07-28
### Added
- Enabled adding and viewing patient feedbacks

## [0.6.2] 2018-07-22
### Added
- Hindi Trnaslation (In progress)

## [0.6.1] 2018-07-16
### Added
- We now have a new logo and app icon!  
See Pull Request #5

## [0.6.0] 2018-07-03
### Added
- A summary of recent healings (today, this month and last month) is shown on each healing log

## [0.5.0] 2018-07-03
### Added
- Added collapsing toolbar with information on recent healings (today and yesterday)
- Added more Analytics events

## [0.4.0] 2018-06-30
### Added
- Edit patient name, contact, rate, payment due and disease.  
** Note - rate edited will take effect from next healing only. Deleting old healings after editing rate may cause unexpected outcomes.
- Added Snackbar confirmations at a few more place!

## [0.3.0] 2018-06-27
### Added
- Added contextual menus in all 3 lists - main patients, healings and payments to be able to delete any incorrect entry.
- Added decoration divider to mark each item separately
### Changed
- Normalized app bar all across the app
### Fixed
- Significantly reduced APK size by enabling minify

## [0.2.0] 2018-06-25
### Added 
- 'Add Payment' option
- Added Payment Logs
- Added Firebase analytics and Crashlytics

## [0.1.0] 2018-06-24
### Added 
- Google sign In
- Add new patients
- View patient info
- Add new healing record for patient
- View healing logs
