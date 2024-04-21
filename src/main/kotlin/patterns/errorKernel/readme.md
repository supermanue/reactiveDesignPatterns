# Error Kernel Pattern

Chapter 12.2

## Intro

In this pattern, the different components are isolated. The idea is that some pieces of our infrastructure are more
failure-prone than others and that pieces are isolated so a failure doesn't affect the rest.

## Flows

- Store a new job.
    - the client
        - creates a job
        - uses ClientInterface module to submit the job to our system using the `ClientInterface.submit` method
            - it must now include the clientId, as the client won't be inmediatle receiving a validation result.
        - queries the ClientInterface about the validation of their jobs with `ClientInterface.getJobValidations`
    - the ClientInterface
        - stores the job in the Storage module with `Storage.store`
        - creates metadata for the jobl. It includes the jobId and clientId
        - informs the JobScheduling module about this job with `JobScheduling.accept`
    - the JobScheduling module
        - validates the job asynchronously with a cron call, `JobScheduling.validateAll`
            - if it is a valid Job it's added to the list of jobs to be planned
            - it if it not valid it is removed from the Storage module
        - stores the result of this validation
        - plans the job asynchronously with a cron call, `JobScheduling.plan`

- Execute a job
    - the Execution module asks the JobScheduling module for the Id of the job to be executed
      with `JobScheduling.nextJob`
    - the JobScheduling gets if from the list of jobs to be executed, `JobScheduling.scheduled`
    - the Execution asks Storage for the job with `Storage.get(jobId)`
    - after returning the job, the Storage removes it from its job storage data structure
        - the Execution gets its first IDLE worker and tells it to run the job with `Worker.execute`

- Scale
    - the execution module monitors the list of pending jobs and the number of Workers.
        - If the list grows too much and the workers can't keep the pace, a new one is created
        - If the list is mostly empty, some of the workers is destroyed

## Implementation and considerations
In this implementation every module run asynchronously in different fibers. They are responsible for creating the own
cron jobs and having them run forever

Within each module, each subcomponent works asynchronously. To communicate and store the information we're using queues.

The idea is that most pieces can fail without consequences. In particular, within the JobScheduling module both
submodules (Scheduling and Validation) have no state. The Workers of the Execution module are stateless too.