import io
import socket
from urllib.request import urlopen
from urllib.error import URLError, HTTPError

socket.setdefaulttimeout( 23 )  # timeout in seconds

def ping(url:'http://google.com'):

	try :
		response = urlopen(url)
	except HTTPError as e:
		print('The server couldn\'t fulfill the request. Reason:', str(e.code))
	except URLError as e:
		print('We failed to reach a server. Reason:', str(e.reason))
	else :
		html = response.read()
		return True
		
	return False
	
urlMap = {}

file = io.open('studipServerAddresses.conf', mode='r', encoding="utf-8")
lines = file.read().strip().split('\n')

for line in lines:
	split = line.split('=')
	urlMap[split[0]] = split[1]

for name, url in urlMap.items():
	print(name, url, ping(url))
	
