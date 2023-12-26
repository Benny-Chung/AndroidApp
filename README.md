# AndroidApp

Demo video: https://youtu.be/pdK2I9VicK4

A nutritient tracker app written in Kotlin. The app includes many features including a dashboard displaying daily & weekly intake summaries with plots, an AI nutritionist powered by ChatGPT, a variety of input options implemented with advanced techniques like image classification (with Tensorflow Hub model), and a favourite food list for storing food. The database is implemented via Android's build-in SQLite for local storage and AWS RDS for online storage.

Note that the used online services are terminated, so simply running this repository will not work properly. This repository sorely acts as a code demostration of the work done.

## Features
Dashboard
- Data visualization on nutrient intakes today and weekly average
- Radar plot for intake overview
- Figures of each nutrient in boxes - Tap on each box to view the weekly intake trend
- Interacterable taken food records at the bottom

Food input options
- Manual search from online database (hosted on AWS RDS)
- Input a food name and find nutrient info via ChatGPT
- Take a photo of the food to be classified
- Scan a barcode of packaged food

AI Nutritionist (powered by ChatGPT)
- Can act as a calorie calculator, diet recommender, etc.
- Able to generate response catered for user's given info (e.g. weight, exercise level) in their profile.
- Reject any unrelated question

Favourite food list
- User can save favourite food and view its nutrient info
- The list can be shared with and imported from others
