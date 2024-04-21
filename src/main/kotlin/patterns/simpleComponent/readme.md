# Simple Component Pattern

Chapter 12.1 

## Intro
This is the most basic approach. We have several components, each with a simple and well-defined responsibility.

- Client: this is the client for our system. It represents the final user
- Client Interface: presents and endpoint that the clients can use; interacts with the rest of the system on behalf of the clients
- Job Scheduling: plans the execution of jobs. It is composed by two submodules
  - Validation: makes sure that the jobs are correct
  - Planning: determines what job to execute
- Execution: executes a job
- Storage: stores information related to jobs.

Here every component is as simple as possible. This is the most basic pattern to follow and is universally applicable. Its application leads to a fine-grained division of responsibility. 

It helps on the later phases of design and implementation, as it makes obvious where to place certain functionality or if a new component ought to be added.  

## Flows
There are two distinct flows.

- Store a new job.
  - the client creates a job
  - the client uses ClientInterface module to submit the job to our system using the `ClientInterface.submit` method
  - the ClientInterface
    - validates the job using `JobScheduling.validate`
    - if it is valid, the job is stored in the Storage module with `Storage.store`
- Execute a job
  - the Execution module asks the JobScheduling module for the Id of the job to be executed with `JobScheduling.nextJob`
  - the JobScheduling uses its submodule `Scheduling` to get the Id of the job with `Scheduling.nextJob`
  - the Scheduling calls Storage to get all the jobs with `Storage.allJobs` and uses this information to determine the next one. This Id is returned to the Execution
  - the Execution asks Storage for the job with `Storage.get(jobId)` and executes it
  - after returning the job, the Storage removes it from its job storage data structure