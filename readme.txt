#README

#Description
the server is a multithreaded webserver based on what we have learnt. it can receive simultaneous requests
it can be considered a HTTP 1.1 server as it can only assess GET, HEAD and if-modified-since requests.
last-modified header is also included in response. the main java code is named mywebs.java. note that the server 
does not support persistent connection.

#installation and usage
1.Code can be compiled and executed using the attached batch file. 
2.After the first pause (indicating that it finished compiling), 
3.press any button on the cmd to run the webserver. 
4.The webserver can receive multiple requests and will not terminate the program after running.
5.requests are to be made to http://127.0.0.1:8080/<file> 
6. closing the batch terminal will terminate the program

the possible status codes sent by the program are:

200- OK
404- NOT FOUND
400- BAD REQUEST
304- UNMODIFIED RESPONSE

code 304 will be sent if the request contains a If-Modified-Since header and its later than the 
Last-modified of the file requested and hence no body will be sent. if not, a 200 error will be sent.

code 200 is sent when a request is fulfilled without error
code 400 will be sent if the request cant be read. along with a HTML error page
code 404 will be sent if the file requested is not found. along with a HTML error page

##log file
The data is logged to a file named log.txt. the format is shown below:

Mon, 27 Apr 2020 10:28:44 GMT, client IP:/127.0.0.1:61293, 404, GET /helloworghld.html HTTP/1.1
Order: Time in GMT, client IP, status code, request line

##appendix
If-modified-since header tested using postman application. 
all time and dates are in HTTP compliant format and are in GMT time zone
code can also be run on intelij and eclipse and will still accept requests


