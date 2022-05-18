# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0-beta06] - 2022-05-18
### Added
- Added a brand new splash screen

## [2.0.0-beta05] - 2022-02-27
### Added
- Context menus on home screen to make it easier to edit and delete activities
- Added shortcuts and integration with Google Assistant to make it easier to use the app!

### Changed
- Updated time handling across the app

### Fixed
- Fixed bug related to invalid time when exporting data

## [2.0.0-beta04] - 2021-11-27
### Added
- Added unlimited scrolling list on Dashboard!

### Changed
- Memory optimizations for different lists across app
- Faster query for stats on dashboard

## [2.0.0-beta03] - 2021-06-02
### Added
- Added local backup + restore (CSV) ability.

## [2.0.0-beta02] - 2021-04-12
### Added
- Patient created events now show up in dashboard

## [2.0.0-beta01] - 2021-03-15
Same as [2.0.0-alpha06]

## [2.0.0-alpha06] - 2021-03-06
### Added
- Edit implementation for healings and payments
- Core module for common database, utils and more
- Analytics event tracking
- [META] Github actions workflow for regular builds and automated releases
- Unit tests for CalendarUtils
- Some database integration tests

### Changed
- Datastore refactor

### Fixed
- Fixed navigation graph and popup behaviour.

## [2.0.0-alpha05] - 2021-02-28
### Added
- Added Undo action for Snackbar on deleting Healing and Payment
- A new material design for Progressbar, TimePicker, DatePicker

### Changed
- Switched to using transactions for deleting healings and payments.

### Fixed
- Fixed a bug where deleting multiple healings/payments did not correctly update the due amount.

## [2.0.0-alpha04] - 2021-02-21
### Changed
- Changed splash screen behaviour
- Updated dependencies
- Added separators for Healings and Payment list

## [2.0.0-alpha03] - 2021-02-16
### Added
- Import from v1.0 and Clear all options in Settings

## [2.0.0-alpha02] - 2021-02-08
### Added
- Individual healings can now be customized to include a different price
- Migration option from v1.0
- New local database

### Changed
- Migrated to local database
- A completely new design

### Removed
- Authentication requirement
- Online database support
- Patient feedbacks

### Security
- Data now does not leave user's device

## [1.0.0] - 2019-08-16
### Added
- Added donate option
### Changed
- Fixed bug with healing counter on main page
- Internal changes (Switched from Java to Kotlin)

## [0.10.0] - 2018-11-02
### Added
- Added option to set date and time for new healings.
- Added some cool new icons.
- Added fragment back stack.
- Added option to set date and time for new payments.
### Changed
- Changed upgrade to donate.

## [0.9.0] - 2018-10-31
### Added
- Added an extra line after name in the main list to display additional information.
- Added settings menu.
- Added option to disable crash reporting.
- Added list of open source licenses.
- Added link to privacy policy and EULA in about section.

### Changed
- Larger font size for patient name.
- Enabled sharing.
- Collapsing Toolbar and new patient FAB only appear in the main list. The other views have their own standard toolbar.
- Updated to latest versions of various libraries and plugins.

### Fixed
- Changed package names to lowercase to follow standard conventions (internal change, wont affect app performance).

### Removed
- Removed redundant share option in navigation drawer.
- Billing permission as not needed.

## [0.8.2] - 2018-10-20
### Fixed
- Fixed app crashing when number was not formatted properly in the due and rate fields.

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

[Unreleased]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-beta06...HEAD
[2.0.0-beta06]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-beta05...v2.0.0-beta06
[2.0.0-beta05]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-beta04...v2.0.0-beta05
[2.0.0-beta04]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-beta03...v2.0.0-beta04
[2.0.0-beta03]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-beta02...v2.0.0-beta03
[2.0.0-beta02]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-beta01...v2.0.0-beta02
[2.0.0-beta01]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-alpha06...v2.0.0-beta01
[2.0.0-alpha06]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-alpha05...v2.0.0-alpha06
[2.0.0-alpha05]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-alpha04...v2.0.0-alpha05
[2.0.0-alpha04]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-alpha03...v2.0.0-alpha04
[2.0.0-alpha03]: https://github.com/yashovardhan99/HealersDiary/compare/v2.0.0-alpha02...v2.0.0-alpha03
[2.0.0-alpha02]: https://github.com/yashovardhan99/HealersDiary/compare/v1.0.0...v2.0.0-alpha02
[1.0.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.10.0...v1.0.0
[0.10.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.9.0...v0.10.0
[0.9.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.8.2...v0.9.0
[0.8.2]: https://github.com/yashovardhan99/HealersDiary/compare/v0.8.1...v0.8.2
[0.8.1]: https://github.com/yashovardhan99/HealersDiary/compare/v0.8.0...v0.8.1
[0.8.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.7.0-alpha.9...v0.8.0
[0.7.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.6.2-alpha.8...v0.7.0-alpha.9
[0.6.2]: https://github.com/yashovardhan99/HealersDiary/compare/v0.6.1-alpha7...v0.6.2-alpha.8
[0.6.1]: https://github.com/yashovardhan99/HealersDiary/compare/v0.6.0-alpha.6...v0.6.1-alpha7
[0.6.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.5.0-alpha.5...v0.6.0-alpha.6
[0.5.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.4.0-alpha.4...v0.5.0-alpha.5
[0.4.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.3.0-alpha3...v0.4.0-alpha.4
[0.3.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.2.0-alpha.2...v0.3.0-alpha3
[0.2.0]: https://github.com/yashovardhan99/HealersDiary/compare/v0.1.0-alpha.1...v0.2.0-alpha.2
[0.1.0]: https://github.com/yashovardhan99/HealersDiary/releases/tag/v0.1.0-alpha.1
