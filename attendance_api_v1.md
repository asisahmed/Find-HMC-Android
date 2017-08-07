AbilityLoft Attendance API v1.1
We want to implement a simple app to let all our employees track their attendance times. We created a rough description within the attached file, I hope the whole concept is understandable. 


Thanks,
André


database schemes (6 schemes):
=================
1. users 
  user_id     int, not null
  email       string, not null
  firstname   string
  lastname    string
  language    string
  image       base64 string or iamge-blob
  manager_id  int, linked to other user or null
  group       either "employee" or "manager"

2. time_entries
  user_id       int, linked to user, not null
  uuid          string, unique, not null, set by the server
  entrytime     datetime, not null
  state         string, "in" or "out", not null
  created_with  string, not null
  description   string
  related       string
  valid         string, "valid", "rejected" or "validation_required", not null

3. time_calculations
  day           date, not null
  user_id       int, linked to user, not null
  in            datetime
  out           datetime
  breakminutes  int
  workminutes   int
  status        string, "valid", "rejected" or "validation_required", not null
  description   string, concatination of the description fields by the corresponding time_entries
  *Constraint: day+user_id-Combination must be unique

4. overtime
  user_id           int, linked to user, not null
  week              int (1-52)
  workhours_plan    int
  workhours_actual  int
  status            string

5. vacation <- no api access required yet, entries will be entered manually
  user_id           int, linked to user, not null      
  vacation_start    date, not null
  vacation_end      date, not null
  status            string
  approved_by       int, linked to other user (manager)

6. holidays <- no api access required yet, entries will be entered manually
  holiday_name      string
  holiday_date      date


timesheet-api 
==============
## User data (5 methods):
1. Authenticate with Google GSuite (e.g. oAuth)
2. Destroy the session
3. get user data
GET /api/v1/me
{
	"data": {
		"id":123,
		"email":"john.doe@abilityloft.com",
		"firstname":"John",
        "lastname":"Doe",
		"language":"de_DE",
		"image":"data:image/gif;base64,R0lGODlhAQABAIAAAP///////yH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==",
        "workhours":40
		"manager_id":"124",
        "manager_firstname":"124",
        "manager_lastname":"124",
        "group":"employee"
}


4. set user data
PUT /api/v1/me

A user can update the following fields:
email: string, valid email
firstname:  string
lastname:   string
language:   string

5. get all users
GET /api/v1/users
To get a successful response, the user must be manager. A successful response is an array of workspace users:
[
	{
		"id":123,
		"email":"john.doe@abilityloft.com",
		"firstname":"John",
        "lastname":"Doe",
		"language":"de_DE",
		"image":"data:image/gif;base64,R0lGODlhAQABAIAAAP///////yH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==",
		"manager_id":"124",
        "manager_firstname":"124",
        "manager_lastname":"124",
        "group":"employee"
	},{
		"id":124,
		"email":"al@abilityloft.com",
		"firstname":"Andre",
        "lastname":"Lung",
		"language":"de_DE",
		"image":"data:image/gif;base64,R0lGODlhAQABAIAAAP///////yH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==",
		"manager_id":null,
        "manager_firstname":null,
        "manager_lastname":null,
        "group":"manager"
	}
]



## time entries (7 methods)
The requests are scoped with the user who is logged in. Only his/her time entries are updated, retrieved and created.
A time entry has the following properties
entrytime:    time entry start or end time (datetime)
state:        "in" or "out" (string, required)
created_with: the name of your client app (string, required)
description:  an optional note (string)
uuid:         the own UUID of this entry, (string, set by the server)
related:      the UUID of the related "in"/"out" entry
valid:        "valid", "rejected" or "validation_required" (string, set by the server)


1. start an entry  
POST /api/v1/time_entries/in
can only be started, if no other entry is "running" (# of in = # of out). state, entrytime, uuid and valid are set by the server.
Required fiels: created_with
Successful response:
{
    "data":
    {
        "uuid":"550e8400-e29b-11d4-a716-446655440000"
    }
}

2. stop an entry   
POST /api/v1/time_entries/out
can only be started, if another entry is "running" (# of (in-1) = # of out). state, entrytime, uuid and valid are set by the server.
Required fiels: created_with
Successful response:
{
	"data":
	{
		"uuid":"550e8400-e29b-11d4-a717-446655440000"
        "related_uuid":"550e8400-e29b-11d4-a716-446655440000"
	}
}

3. manually create an entry
POST /api/v1/time_entries
Manually created entries are flagged (valid:validation_required), so they can be controlled by a manager.
Required fields: entrytime, state, created_with, description
Successful response
{
    "data":
    {
        "uuid":"550e8400-e29b-11d4-a716-446655440000"
    }
}


4. Get time entry details
GET /api/v1/time_entries/{time_entry_uuid}
Successful response
{
	"data":{
        "entrytime":"2017-02-27T01:24:00+00:00",
        "state":"in",
        "created_with":"desktop",
        "description":"I forgot to punch in, sorry",
        "uuid":"550e8400-e29b-11d4-a716-446655440000",
        "related":null,
        "valid":"validation_required"
	}
}


5. Update a time entry
PUT /api/v1/time_entries/{time_entry_uuid}
Payload+Response
{
	"data":{
        "entrytime":"2017-02-27T01:24:00+00:00",
        "state":"in",
        "created_with":"desktop",
        "description":"I forgot to punch in, sorry",
        "uuid":"550e8400-e29b-11d4-a716-446655440000",
        "related":null,
        "valid":"validation_required"
	}
}


6. Delete a time entry
DELETE /api/v1/time_entries/{time_entry_uuid}
this sets the corresponding entry to "rejected"
Successful request will return 200 OK



7. Get time entries started in a specific time range
GET /api/v1/time_entries
With start_date and end_date parameters you can specify the date range of the time entries returned. If start_date and end_date are not specified, time entries started during the last 30 days are returned. start_date and end_date must be ISO 8601 date and time strings.

Example request with start date 2017-07-10T15:42:46+02:00 and end_date 2017-07-12T15:42:46+02:00
GET "/api/v1/time_entries?start_date=2017-07-10T15%3A42%3A46%2B02%3A00&end_date=2017-07-12T15%3A42%3A46%2B02%3A00"
Successful response
[
	{
		"entrytime":"2017-07-11T07:24:00+00:00",
        "state":"in",
        "created_with":"desktop",
        "description":"I forgot to punch in, sorry",
        "uuid":"550e8400-e29b-11d4-a716-446655440000",
        "related":"550e8400-e29b-11d4-a717-446655440000",
        "valid":"validation_required"
	},{
		"entrytime":"2017-07-11T16:04:00+00:00",
        "state":"out",
        "created_with":"mobile",
        "description":null,
        "uuid":"550e8400-e29b-11d4-a717-446655440000",
        "related":"550e8400-e29b-11d4-a716-446655440000",
        "valid":"valid"
	}
]



Report-API (2 methods)
======================
Reports are used to display the logged time. Use cases are: 
+ get a single report of the current logged in user's entries as overview for a single user     <- defined here
+ get many reports for a manager (of all his subordinates)                  <- same as the above but grouped by employees

1. get per day report (employee)
GET /api/v1/reports/perday
With start_date and end_date parameters you can specify the date range of the time entries returned. If start_date and end_date are not specified, all time entries for the current week are returned. start_date and end_date must be ISO 8601 date and time strings.
This will be used for a list like this:
    #date         #in       #out      #breaktime    #workhours    #status       #note
    2017/07/17    7:20      17:20     30            9:30          approved
    2017/07/18    7:50      17:20     30            9:00          pending       I forgot to punch in, sorry

This report should query the time_calculations. Logic proposal: whenever a time entry is logged/changed in time entries, this time+uuid combination should also be created/updated in time_calculations.

Successful response:
[
	{
		"date":"2017-07-17",
        "in":"07:20:00+00:00",
        "out":"17:20:00+00:00",
        "breaktime":"30",
        "workhours":"9:30",
        "status":"approved",
        "description":null
	},{
		"date":"2017-07-18",
        "in":"07:50:00+00:00",
        "out":"17:20:00+00:00",
        "breaktime":"30",
        "workhours":"9:00",
        "status":"pending",
        "description":"I forgot to punch in, sorry"
	}
]

2. get overtime report (employee)
GET /api/v1/reports/overtime?year
returns an overview for the whole year, ordered by year
Successful response:
[
	{
        "week":1,
        "workhours_plan":40,
        "workhours_actual":41,
        "status":"approved"
    },
    {
        "week":2,
        "workhours_plan":40,
        "workhours_actual":32,
        "status":"approved"
    }
]