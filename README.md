# `subra-examples`
> Reference implementation for several subra applications 

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2e361a22754a4f749f44cf6eb5153c55)](https://www.codacy.com/app/delta-leonis/subra-examples?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=delta-leonis/subra-examples&amp;utm_campaign=Badge_Grade)
[![CircleCI](https://circleci.com/gh/delta-leonis/subra-examples.svg?style=svg)](https://circleci.com/gh/delta-leonis/subra-examples)

You'll need at least Java 1.8 ([jre](https://www.java.com/download/)
/[jdk](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html))
to run `subra-examples`.

## Documentation

The javadoc for the current code on `master` can be found on https://delta-leonis.github.io/subra-examples/

## Building

Make sure you have `gradle>=v2.10` installed. Run the following to build the application:

```
gradle build
```

## Configuration

Most examples accept arguments in the following format: `<property>:<value>`. For example, setting
the IP would be `ip:123.123.123.123`.

## Controller Example

The class `ControllerExample` can be found in the example package. Currently the controller mapping
is hardcoded in the `main`-method of the example, which binds robot 1 and 2 of the blue team to the
first controller that is found.

### Settings

| Property | Description    | Default     |
|--------- |--------------- |------------ |
| `ip`     | Multicast IP   | `224.0.0.1` |
| `port`   | Multicast port | `10001`     |

## Copyright

This project is licensed under the AGPL version 3 license (see LICENSE).

```
subra-examples - delta-leonis
Copyright (C) 2017 Rimon Oz, Jeroen de Jong, Ryan Meulenkamp, Thomas Hakkers

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
