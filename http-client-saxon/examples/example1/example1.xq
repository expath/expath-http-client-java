xquery version "1.0";

declare namespace http = "http://expath.org/ns/http-client";

let $my-request := <http:request href='https://www.google.com' method='get'/>
return
    http:send-request($my-request)