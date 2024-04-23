Add-Type -Path "C:\chikat\ChilkatDotNet47.dll"

# This example assumes the Chilkat HTTP API to have been previously unlocked.
# See Global Unlock Sample for sample code.

$http = New-Object Chilkat.Http

# Using a SOCKS5 proxy is just a matter of setting a few properties.
# Once these properties are set, all other coding is the same as when
# the connection is direct to the HTTP server.

# Set the SocksVersion property = 5 for SOCKS5
$http.SocksVersion = 5

# Set the SocksHostname to the SOCKS proxy domain name or IP address,
# which may be IPv4 (dotted notation) or IPv6.
$http.SocksHostname = "109.172.5.186"

# The port where the SOCKS5 proxy is listening.
$http.SocksPort = 59101

# If the SOCKS5 proxy itself requires authentication, set the username/password
# like this.  (otherwise leave the username/password empty)
$http.SocksUsername = "rlukashenko0tGVv"
$http.SocksPassword = "5aBpfwzTSv"

# Now do whatever it is you need to do.  All communications will go through the proxy.
$html = $http.QuickGetStr("https://www.google.com/")
if ($http.LastMethodSuccess -ne $true) {
    $($http.LastErrorText)
    exit
}

$($html)
$("----")
$("Success!")

ohip13ip2vcb5dufy2a8net
RNW78Fm5