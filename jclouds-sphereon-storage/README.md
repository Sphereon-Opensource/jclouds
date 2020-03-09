Sphereon Storage API jclouds integration

This bundle integrates jclouds blobstore functionality with the Sphereon Storage API. It allows you to create, update and delete objects/files and folders in backends supported by Sphereon.

Domain objects are automaticaly generated using swagger using profile 'generate-models'. This profile is enabled by default.


### Maven: Build yourself
```shell
mvn clean install
```

### Maven: Use provided repository

Add this dependency to your project's POM for the java8 domain classes:

```xml
...
<repositories>
    <repository>
        <id>sphereon-public</id>
        <name>Sphereon Public</name>
        <url>http://nexus.qa.sphereon.com/repository/sphereon-public/</url>
    </repository>
</repositories>

...

<dependency>
    <groupId>org.jclouds</groupId>
    <artifactId>jclouds-sphereon-storage</artifactId>
    <version>2.1.3</version>
    <scope>compile</scope>
</dependency>
...
```

## Author
Copyright 2020, Sphereon B.V. <https://sphereon.com>

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

