#include <iostream>
#include <cmath>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/extensions/XTest.h>

using namespace std;

unsigned int uintFromDecString(const char*string, unsigned int length) {
  unsigned int value = 0;
  unsigned int weight = 1;
  for(int i=(length-1); i>=0; --i) {
    char c = string[i];
    if(c>='0' && c<='9') value += (c-'0')*weight;
    weight *= 10;
  }
  return value;
}
int uintFromHexString(const char*string, unsigned int length) {
  unsigned int value = 0;
  unsigned int weight = 1;
  for(int i=(length-1); i>=0; --i) {
    char c = string[i];
    if(c>='0' && c<='9') value += (c-'0')*weight;
    else if(c>='A' && c <='F') value += (c-'A'+10)*weight;
    else if(c>='a' && c <='f') value += (c-'a'+10)*weight;
    weight *= 16;
  }
  return value;
}

int signedFromUnsigned(unsigned int i, unsigned int size) {
  unsigned int p = 1 << (size);
  if(i & (p>>1)) return -( (i^(p-1)) +1);
  else return i;
}
void mouseClick(Display *display, int button)
{

	XEvent event = {0};
	
	if(display == NULL)
	{
		cerr << "Invalid display" << endl;
		return;
	}
	
	
	event.type = ButtonPress;
	event.xbutton.button = button;
	event.xbutton.same_screen = True;
	
	XQueryPointer(display, RootWindow(display, DefaultScreen(display)), &event.xbutton.root, &event.xbutton.window, &event.xbutton.x_root, &event.xbutton.y_root, &event.xbutton.x, &event.xbutton.y, &event.xbutton.state);
	
	event.xbutton.subwindow = event.xbutton.window;
	
	while(event.xbutton.subwindow)
	{
		event.xbutton.window = event.xbutton.subwindow;
		
		XQueryPointer(display, event.xbutton.window, &event.xbutton.root, &event.xbutton.subwindow, &event.xbutton.x_root, &event.xbutton.y_root, &event.xbutton.x, &event.xbutton.y, &event.xbutton.state);
	}
	
	if(XSendEvent(display, PointerWindow, True, 0xfff, &event) == 0) cerr << "XSendEvent error" << endl;
	
	XFlush(display);
	
	usleep(100000);
	
	event.type = ButtonRelease;
	event.xbutton.state = 0x100;

	if(XSendEvent(display, PointerWindow, True, 0xfff, &event) == 0) cerr << "XSendEvent error" << endl;
	
	XFlush(display);
	
	XCloseDisplay(display);
}

int main(int argc, char *argv[]) {
    Display *dpy;
    Window root_window;

    dpy = XOpenDisplay(0);
    root_window = XRootWindow(dpy, 0);
    XSelectInput(dpy, root_window, KeyReleaseMask);

    int ev, er, ma, mi;
    if(!XTestQueryExtension(dpy, &ev, &er, &ma, &mi))
    {
        cerr << "XTest extension not supported on server." << endl;
        return 1;
    }
    cout << "XTest for server \"" << DisplayString(dpy) << "\" is version " << ma << "." << mi << endl;

    
    sockaddr_in serverAddr = {0};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    serverAddr.sin_port = htons(8500);
    
    int serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    bind(serverSocket, (sockaddr*)&serverAddr, sizeof(serverAddr));

    listen(serverSocket, 10);
    
    while(true) {
    
      sockaddr_in client_addr = {0};
      socklen_t addr_size = sizeof(client_addr);
      int socket = accept(serverSocket, (sockaddr*)&client_addr, &addr_size);
      char ip[INET_ADDRSTRLEN] = {0};
      inet_ntop(AF_INET, &(client_addr.sin_addr), ip, INET_ADDRSTRLEN);
      bool connected = socket > 0;
      std::cout << "New client [" << ip << "]" << std::endl;
      //send(socket, "Hello world!", 12, 0);
      char buffer[6];
      while(connected) {
        int n = recv(socket, buffer, 6, 0);
        
        connected = n > 0;
        //std::cout << "[recv" << n << "]" << buffer << std::endl;
        if(n==6) {
          if(buffer[0]=='m') {

            if(buffer[1]=='c') {
              int button = buffer[2]=='r' ? 3 : 1;
              XTestFakeButtonEvent(dpy, button, True, CurrentTime);
              XTestFakeButtonEvent(dpy, button, False, CurrentTime);
            }
            else if(buffer[1]=='b') {
              XTestFakeButtonEvent(dpy, 1, buffer[2]=='d' ? True : False, CurrentTime);
            }
            else if(buffer[0]=='s') {
              int button = (buffer[2]=='u') ? 4 : 5;
              XTestFakeButtonEvent(dpy, button, True, CurrentTime);
              XTestFakeButtonEvent(dpy, button, False, CurrentTime);
            }

            XFlush(dpy);
          }
          else {
            unsigned int udx = uintFromHexString(buffer, 3);
            unsigned int udy = uintFromHexString(buffer+3, 3);
            int dx = signedFromUnsigned(udx,12);
            int dy = signedFromUnsigned(udy,12);
            
            int mx = (dx*dx)/10;
            if(dx<0) mx = -mx;
            int my = (dy*dy)/10;
            if(dy<0) my = -my;
            XWarpPointer(dpy, None, None, 0, 0, 0, 0, dx, dy);
            XFlush(dpy);
          }
          
          //std::cout << dx << ":" << dy << std::endl;
        }
      }
      close(socket);
    
    }
    
    return 0;
}
