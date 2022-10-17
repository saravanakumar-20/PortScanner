import java.io.IOException;
import java.util.Stack;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Scanner;
 class PortScannerOld {
     public static void main(String[] args) {
	  Scanner console = new Scanner ( System.in );
	  int choice = 0;
	  List < String > ipAddress = new ArrayList < > ( );
	  do {
	   System.out.print ( "1.single ip\n2.using subnet mask\n3.range of ip\n4.list of ip\n5.exit\nchoice: " );
	   choice = console.nextInt ( );
       if ( choice == 1 ) {
		 System.out.print ( "ip address: " );
		 ipAddress.add ( console.next ( ) );
       }
	   else if ( choice == 2 ) {
		  System.out.print ( "sample ip: " );
		  console.nextLine ( );
		  String ip = console.next ( );
		  System.out.print ( "subnet mask: " );
		  String mask = console.next ( );
		  String [] ipRange = getStartingIp ( ip ,  mask );
          long sourceNumber = getAddressNumber ( ipRange [ 0 ] );
		  long destNumber = sourceNumber + (long) Math.pow ( 2 , Integer.parseInt ( ipRange [ 1 ] ) ) - 2;
		  do {
			ipAddress.add ( getAddress ( ++ sourceNumber ) );
		  } while ( sourceNumber != destNumber );
	   }
       else if ( choice == 3 ) {
		  System.out.print ( "starting ip: " );
		  console.nextLine ( );
		  String source = console.next ( );
		  System.out.print ( "ending ip: " );
		  String dest = console.next ( );
          long sourceNumber = getAddressNumber ( source ) - 1;
		  long destNumber = getAddressNumber ( dest );
		  do {
			  ipAddress.add ( getAddress ( ++ sourceNumber ) );
		  } while ( sourceNumber != destNumber );
	   }
	   else if ( choice == 4 ) {
		  System.out.print ( "list of ip addresses separated by comma: " );
		  console.nextLine ( );
		  String [] line = console.nextLine ( ).split ( "," );
		  for ( String ip: line )
		    ipAddress.add ( ip );
	   }
	   else if ( choice == 5 )
	     break;
	   else {
		  System.out.print ( "invalid choice!" ); 
		  continue;
	   }
	   System.out.println ( "--------------Started Scanning--------------" );
	   for ( String ip: ipAddress )
	   {
		  System.out.println ( ip + ": " );
		  List openPorts = portScan ( ip );
		  if ( openPorts.isEmpty ( ) )
		    System.out.println ( "\nno port is in open state!" );
		  else {
		    System.out.println ( );
		    openPorts.forEach(port -> System.out.println("port " + port + " is open"));
		  }
	   }
	   ipAddress.clear ( );
	   System.out.println ( "\n--------------Finished Scanning-------------" );
	  } while ( choice != 4 );
    }
    public static String [] getStartingIp ( String ipAddress , String networkMask  ) {
      String [] ipParts = ipAddress.split ( "\\." );
      String [] maskParts = networkMask.split ( "\\." );
      StringBuilder startingIp = new StringBuilder ( "" );
      int count = 0;
      for ( int index = 0; index < ipParts.length; index ++ ) {
        int part1 = Integer.parseInt ( ipParts [ index ] );
        int part2 = Integer.parseInt ( maskParts [ index ] );
        startingIp.append ( ( part1 & part2 ) + "." );
        count += ( getBitCount( Integer.parseInt ( "255" ) ^ part2 ) );
      }
      return new String [] { startingIp.substring ( 0 , startingIp.length ( ) - 1 ) , count + "" };
    }
    public static int getBitCount ( int number ) {
      int count = 0;
      while ( number != 0 )
      {
        count += number % 2 == 1 ? 1 : 0;
        number /= 2;
      }
      return count;
    }
    public static String getAddress ( long number  ) {
      Stack < Long > stack = new Stack < > ( );
      while ( number != 0 ) {
        stack.push ( number % 256 );
        number /= 256;
      }
      StringBuilder ipAddress = new StringBuilder ( "" );
      while ( ! stack.empty ( ) )
        ipAddress.append ( stack.pop ( ) + "." );
      return ipAddress.substring ( 0 , ipAddress.length ( ) - 1 );
    }
    public static long getAddressNumber ( String ipAddress ) {
      String [] parts = ipAddress.split ( "\\." ); 
      int count  = 3;
      double data = 0;
      for ( String part: parts )
        data += Math.pow ( 256 , count -- ) * Integer.parseInt ( part );
      return ( long )data;
    }
     public static List portScan( String ip ) {
		 char[] animationChars = new char[]{'|', '/', '-', '\\'};
         ConcurrentLinkedQueue openPorts = new ConcurrentLinkedQueue<>();
		 ConcurrentLinkedQueue closedPorts = new ConcurrentLinkedQueue<>();
         ExecutorService executorService = Executors.newFixedThreadPool(100);
		 executorService.submit ( ( ) -> {
			float loading1 = 0.0f;
			do{
			 loading1 = Float.parseFloat ( String.format ( "%.4f" , ( ( ( openPorts.size ( ) + closedPorts.size ( ) ) * 100.0 ) / 65535 ) ) );
			 System.out.print("\rScanning in progress: " + ( loading1 % 100 ) + "%" );
			} while( ( openPorts.size ( ) + closedPorts.size ( ) ) != 65535 );
         } );
         AtomicInteger port = new AtomicInteger(0);
         while (port.get() < 65536 ) {
             final int currentPort = port.incrementAndGet();
             executorService.submit(() -> {                try {
                     Socket socket = new Socket();
                     socket.connect(new InetSocketAddress(ip, currentPort), 300);
                     socket.close();
                     openPorts.add(currentPort);
                 }
                 catch (Exception e) {
					 closedPorts.add(currentPort);
                 }
            });
         }
         executorService.shutdown();
         try {
             executorService.awaitTermination ( 10 , TimeUnit.MINUTES);
         }
         catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         List openPortList = new ArrayList<>();
         while (!openPorts.isEmpty()) {
             openPortList.add(openPorts.poll());
			        }
         return openPortList;
     }
 
 }