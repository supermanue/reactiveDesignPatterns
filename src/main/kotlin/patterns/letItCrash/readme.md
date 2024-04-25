# Let It Crash Pattern

Chapter 12.3

## Intro

This patter extends the Error Kernel pattern by assuming that _any_ component may fail, not only some of them. Instead
of trying to recover from such failures the system will just kill (or let die) the affected modules and restart them.

To perform this operation safely, we need to assume that the failure may come at any point and react accordingly.

## Implementation and considerations

Files which are not modified from `ErrorKernel` are not duplicated but imported here instead.

As a limitation of the current implementation, here we'll be assuming that all disk-related operations that we perform (
writing and reading from files) can't fail. In the real world this would be a database with strong guarantees.

Also, we're assuming that the Storage module may never fail, as it is basically a wrapper around the DB

How has this patter affected the modules:

- JobMetadata: it is anotated with `Serializable` so it can be converted to/from Json effortlessly
- Client: the clients now need to store their `lastJobId` variable in disk and read it when starting.
- Storage: we have added a new `getIdForNewJob` method to overcome failures in the ClientInterface
- ClientInterface: it could crash in the period between storing the job and informing the JobScheduling. We have
  modified it so the JobScheduling gets the jobId first, and then it is stored. This way, when this situation happens we
  will end with the metadata but no job, a situation that can be easily detected by the JobScheduling.
- JobScheduling: This module is now storing its internal state each time there is a modification. This is read at init
  time, so in the case of a failure it can pick up old tasks. Note that the storage part os not particularly efficient,
  I haven't put effort into that

Also, as we are now exporting the state to files and that's a time-consuming operation, we need to control the access to
those data structures. To do so I've just added a Mutex whenever necessary so there are no concurrency issues. It could
be a more sophisticated solution but I think that's out of the scope of this projecrt