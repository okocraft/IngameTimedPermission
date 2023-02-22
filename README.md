## TimedPerms
TimedPerms, plugin to implements in-game time temporary permission using LuckPerms.

LuckPerms only supports realtime temporary permission. So this plugin supports in-game temporary permission.

The time of temporary permission managed by this plugin will be decreased by 1 on every second only when a player is online.

### Install
build and drop jar in plugins folder. (wip)

### Commands
These commands require permission `timedperms.use`.
* `/timedperms show <player> <permission [context=value...]>`
* `/timedperms add <player> <permission [context=value...]> <time-in-seconds>`
* `/timedperms set <player> <permission [context=value...]> <time-in-seconds>`
* `/timedperms remove <player> <permission [context=value...]> [time-in-seconds]`

### Placeholders (PlaceholderAPI)
Format of placeholders which return time of permission in seconds are:
* with player: `%timedperms_<player>_<permission>_<context=value>_...%`
* with player (comma): `%timedperms_<player>,<permission>,<context=value>,...%`
* without player: `%timedperms_<permission>_<context=value>_...%` (executor required)
* without player (comma): `%timedperms_<permission>,<context=value>,...%` (executor required)

if permission contains underscore, use comma to retrieve data.
even if player name contains underscore, you can use both format.

### For Developers
First, retrieve LocalPlayer from LocalPlayerFactory#get(). Then you can do some stuff with it. Methods which operate time of permission returns calculated new value. Those time operation will fire event below.

TimedPerms have some events:
* `TimedPermissionRegisteredEvent`: Fired when timed permission is registered via adding time.
* `TimedPermissionUnregisteredEvent`: Fired when timed permission is unregistered via removing time or counting.
* `TimedPermissionCountEvent`: Fired when time of permission is decreased by 1 every second.
* `TimedPermissionExpireEvent`: Fired when timed permission is expired via counting. Expire event will be fired after count event.
* `TimedPermissionAddEvent`: Fired when time of permission is increased via adding time.
* `TimedPermissionRemoveEvent`: Fired when time of permission is decreased via removing time.
* `TimedPermissionSetEvent`: Fired when time of permission is set.
