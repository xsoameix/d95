# Getted Started

Run server

    $ java -cp bin server.Main

Run client

    $ java -cp bin client.Main

# Design

Server

    loop
      Server accept new connection until 2 connection arrive
      Server notice worker to run
      Server wait until any worker leave

Worker

    Worker wait until server notice it
    Worker run
      if connection close
        Worker notice server to accept new connection
      if any worker leave
        Worker wait until new worker join
