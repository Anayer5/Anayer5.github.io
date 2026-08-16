# CS-340-18637-M01-Client-Server-Development-2026
contains Course work for CS-340-18637-M01 Client/Server Development 2026
# Project Reflection: Grazioso Salvare Dashboard

## Writing Maintainable, Readable, and Adaptable Programs

**How do you write programs that are maintainable, readable, and adaptable? Especially consider your work on the CRUD Python module from Project One, which you used to connect the dashboard widgets to the database in Project Two. What were the advantages of working in this way? How else could you use this CRUD Python module in the future?**

I write maintainable, readable, and adaptable programs by separating responsibilities, using clear naming conventions, adding useful comments, and organizing code so that each part has one clear purpose. The best example from this course was the CRUD Python module from Project One. Instead of placing database access code directly inside the dashboard, I created a reusable module that handled create, read, update, and delete operations in one place. This made the code easier to maintain because any database change could be made in the module without rewriting multiple dashboard sections. It also improved readability because the dashboard code stayed focused on presentation and interaction, while the CRUD module handled data access.

There were major advantages to working this way in Project Two. The same CRUD module could be imported and reused to connect dashboard widgets to MongoDB, which saved time and reduced repeated code. It also made debugging easier because the database logic was centralized. In the future, this CRUD Python module could be reused in other dashboards, reporting tools, administrative systems, or web applications that need structured access to the same database. It provides a strong foundation that could also be extended with more advanced filtering, validation, or security features.

---

## Approaching Problems as a Computer Scientist

**How do you approach a problem as a computer scientist? Consider how you approached the database or dashboard requirements that Grazioso Salvare requested. How did your approach to this project differ from previous assignments in other courses? What techniques or strategies would you use in the future to create databases to meet other client requests?**

I approach a problem as a computer scientist by first understanding the requirements, then breaking the problem into smaller parts, and finally designing a solution that connects those parts in a logical way. For Grazioso Salvare, I first studied the dashboard requirements, the rescue-type specifications, and the way the MongoDB data needed to support filtering, mapping, and charting. After that, I focused on building the database access layer through the CRUD module, then designed the dashboard interface so the data table, geolocation chart, and pie chart could all respond to user-selected filters.

This approach was different from many earlier assignments because it required more than writing one isolated script. It involved combining database design, Python programming, user interaction, and visualization into one complete client-focused solution. Instead of only solving a technical problem, I had to think about usability, maintainability, and how the client would actually use the system. In the future, I would continue using strategies such as analyzing client requirements first, designing reusable modules, validating data early, and testing each component separately before integrating everything into a complete application. These strategies would help me create databases and systems that better meet different client requests.

---

## The Role and Impact of Computer Scientists

**What do computer scientists do, and why does it matter? How would your work on this type of project help a company, like Grazioso Salvare, to do their work better?**

Computer scientists design systems that turn data into useful information and help organizations solve real-world problems more efficiently. Their work matters because businesses and nonprofits rely on accurate, organized, and accessible information in order to make good decisions. In this project, the dashboard was designed to help Grazioso Salvare quickly identify dogs that match rescue-training requirements by filtering shelter records, displaying them in a usable table, and visualizing them through maps and charts.

This type of work would help a company like Grazioso Salvare do its job better by reducing manual effort and making important data easier to understand. Instead of searching through raw shelter records one at a time, staff can use the dashboard to focus on the dogs that match their rescue categories. That improves speed, consistency, and decision-making. More broadly, this project shows how computer science supports organizations by building tools that make their work more efficient, more accurate, and more practical.
