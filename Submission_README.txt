Eulerity Hackathon Submission
•	Thank you for giving me the opportunity to work on this challenge.
•	It was a fun project to build and I enjoyed working on it.

WORK FLOW : 
1.	User inputs a URL and selects the number of subpages to crawl on the frontend page.
2.	Frontend sends a POST request to /api/crawl with the URL and number of subpages.
3.	Backend starts crawling:
4.	The starting page is crawled for images.
5.	Subpages are discovered from <a> tags.
6.	Subpages are crawled concurrently (parallel threads).
7.	Images found are sent live:
8.	Each image URL found is immediately broadcasted to the frontend through WebSocket.
9.	Frontend displays images in real-time:
10.	Images appear live in a grid.
11.	Users can copy any image URL.
12.	When crawling is finished, a special message "__CRAWL_DONE__" is sent to the frontend to indicate completion.
13.	Frontend shows "Crawl Completed!" and final image count.





Why I Designed Application this way : 
Separate Backend and Frontend Responsibilities
1.	I kept the crawling logic purely on the backend (Java).
2.	The frontend (HTML/JS) only handles user input and displays images.
3.	This separation makes the system easier to maintain and scale later.

Playwright for Crawling
1.	Many modern websites load images dynamically with JavaScript.
2.	Jsoup was not enough (only static HTML parsing).
3.	Selenium worked but was slower and heavier.
4.	Playwright gave the best balance: fast page loading, supports dynamic content, more stable for multi-threaded use.

WebSocket for Live Updates
1.	I initially implemented Server-Sent Events (SSE) for live updates,
but after noticing limitations in event delivery speed and reliability,
2.	I switched to WebSocket for a smoother and more scalable real-time experience.

Multi-threaded Crawling for Subpages
1.	Crawling each subpage one after another would be too slow.
2.	Using parallel threads allowed faster discovery without overloading the server.
3.	I also capped the number of threads to avoid creating too many.

Same-Domain Restriction
1.	To prevent crawling the entire internet accidentally, only subpages from the same domain as the starting URL are crawled.
Dynamic Scrolling
1.	Many sites (especially e-commerce) lazy-load images only when scrolling.
2.	I added automatic page scrolling to make sure those images are also captured.
Simple, Clean Frontend
1.	Built a lightweight UI with Bootstrap 5 and vanilla JavaScript.
2.	Focused on clear user feedback: loading spinner, live image counter, "Crawl Completed" message, and copy button.

Bot Detection Handling
1. Some websites (like Walmart) trigger bot detection mechanisms (e.g., "Press & Hold" screens).
2. I added automatic detection and interaction to bypass basic bot challenges during crawling.

Challenges Faced
1. Handling dynamic JavaScript content required moving from Jsoup to Playwright.
2.Managing crawling performance with subpages (solved using dynamic thread pool).
3.Dealing with lazy-loaded images (solved with auto-scrolling).
4.Switching from SSE to WebSocket for faster and more reliable live updates.
5.Handling invalid or broken links (e.g., empty hrefs, javascript:void(0)).

Future Enhancements( I thought of)
1.Filter visually duplicate images by using image hashing.
2.Implement distributed crawling across multiple machines.





