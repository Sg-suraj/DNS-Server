Simple Recursive DNS Server
This project implements a foundational DNS (Domain Name System) server in Java that acts as a recursive resolver. It also includes a Python-based web backend and a simple HTML/JavaScript frontend to demonstrate the server's functionality in a full-stack environment. The system allows users to resolve domain names to their corresponding IP addresses through a web interface.

Features
Recursive DNS Resolution: The server first checks a local domain-to-IP map for a given query. If the domain is not found, it forwards the query to a public DNS server (e.g., Google's 8.8.8.8) to find the answer.

Local Domain Mapping: Includes a hardcoded HashMap to provide quick resolution for specific, predefined domains.

Error Handling: The server and client correctly handle NXDOMAIN (non-existent domain) responses.

Full-Stack Integration: The project connects a Java networking application to a Python web backend and a user-friendly HTML/JavaScript frontend, showcasing multi-technology development.

UDP Protocol Implementation: Core server logic is built on Java's Datagram Sockets, demonstrating an understanding of the UDP protocol.

Difficulties Faced
Building this project was a fantastic challenge that helped me deeply understand multi-tier application architecture. The primary difficulties were not in the core DNS logic but in making the different components communicate correctly.

Class Not Found Errors: Initially, getting the Java client and server to run was a challenge due to classpath issues. My local IDE and terminal environments were configured differently, leading to persistent NoClassDefFoundError messages that required a thorough understanding of Java's package and classpath system to resolve.

Subprocess Communication Issues: Integrating the Python backend with the Java client proved to be a major hurdle. The Python subprocess command was failing to correctly execute the Java code, resulting in 500 Internal Server Error messages on the frontend. This required careful debugging and a more robust approach using absolute paths and proper input/output handling.

Code Caching: Debugging was made more complex by aggressive code caching in both VS Code and IntelliJ IDEA. My changes were not immediately reflected in the running application, leading to misleading results and requiring me to manually clean and rebuild the project to ensure the latest code was being executed.

Thank you for providing the links to your screenshots on GitHub.

You can now use these URLs to embed the images directly into your README.md file. I've put together the final Markdown code for you below. Simply copy and paste this into your README.md file on GitHub, and the images will display correctly.

How to Run
To run this project, you need three separate terminals or console windows.

Start the Java DNS Server:

Navigate to the project's root folder.

Run the command: java -cp src com.dns.SimpleDNSServer

Start the Python Backend:

In a second terminal, install Flask and dnspython: pip install Flask dnspython

Run the Python backend: python app.py

Open the Frontend:

Open your index.html file in a web browser.

Enter a domain name in the form.

Copyright © 2025 Suraj Kumar
