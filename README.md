cis-manage-frontend
===================

![](https://img.shields.io/github/v/release/hmrc/cis-manage-frontend)

Frontend microservice for the [Construction Industry Scheme (CIS)](https://www.gov.uk/what-is-the-construction-industry-scheme) on the HMRC Tax Platform.

This service allows contractors and agents to:

* View and manage CIS returns
* View subcontractor records and apply automatic updates
* Access monthly return history and amend submissions
* Manage notices and statements
* View and verify client details

## Running the service

Start all dependent services using Service Manager:

```
sm2 --start CIS_ALL
```

To start this service locally on port `6996`:

```
sbt run
```

### Upstream dependencies

| Service                       | Port |
|-------------------------------|------|
| `auth`                        | 8500 |
| `construction-industry-scheme`| 6994 |
| `cis-contractor-frontend`     | 6998 |
| `feedback-frontend`           | 9514 |
| `pas`                         | 8081 |
| `notice-viewer`               | 8080 |

## Testing

Run unit and integration tests with coverage:

```
./run_all_tests.sh
```

Or run tests individually:

```bash
# Unit tests
sbt test

# Integration tests
sbt it/test

# Unit and integration tests with coverage report
sbt clean coverage test it/test coverageOff coverageReport
```

UI tests are maintained in [cis-ui-tests](https://github.com/hmrc/cis-ui-tests).

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
