# Summary for sprint 9

## Ahmed

This week, I continued working on implementing the facebook login feature with Thomas as well as fixing some bug.

I spent a while to discover what was causing the bug; especially that it was coming from a code that I didn't write. After familiarizing myself to this code, I have finally been able to fix the bug (the fix itself is barely a line but took a while to figure out). A take away from this experience is that it is probably more efficient to assign the person who wrote the code to fix the bug (if we know where the bug is sourcing from).

I also worked what I estimated as half of the work for the facebook login and Thomas did the rest. I was then assigned the task to add tests for this code which turned out to be tricky since we don't know how to mock the facebook service.

## Aman

This week, I worked on the poi detection user story. I first used google's Landmark Detection API which took much less time than what I had predicted. Turned out that the API did all of the work and all I needed to do was call the API and handle the response.

However, this API only works for well known landmarks like the EIffel tower but not for more day-to-day POIs such as the EPFL Rolex. I decided to improve poi detection using my own implementation and estimated it would take around 6h, which was the time I had remaning in the sprint. To this end, I used the accelerometer and magnetic field sensors to calculate the orientation of a user's phone. This allowed me to find which POI was nearest in the user's field of view. This now allows the user to see what POI they are looking at just by orienting their phone towards it! This feature took me 7h instead of my 6h estimation because I had to learn more (than expected) about the different layouts in android.

I combined these 2 features into _Meili Lens_ and added them to the main map actiivty so that the user has fast access to poi detection, allowing them to quickly find out the name (and maybe details in the future!) of the POI they are looking at.


## Ewan 
This week I worked on the profile. I improved the UI so that buttons are better placed, and that they are only available when it makes sense. I also made it so that you can click on your friends in your friends list to see their profile (not editable). I'm very happy with my design choices, I think it looks super cool.

As I wanted to make things pretty, I spent extra time on that.

I also had an unexpected error that only happened on Cirrus which made me do overtime (I wouldn't consider that as part of the time estimates, as the tests passed locally). I think the reason was that Cirrus doesn't use the same devices as the android emulator, which makes certain buttons unreachable if we aren't careful with how we implement the UI. That was a learning experience.

Other than that, my time estimates were perfect for the implementation of the features and the tests, which is the most important. I'm happy with my work this week.


## Marcel (Scrum Master)

This week I have worked on implementing 2 tasks. The first was to create a service that would fetch the information from a list of users. This will be used for displaying other users information as for example information about your friends, people with whom you are chating or even posts writers. The second task was to improve the UI of the friends list so that we could see the picture of the friend, its name and that it was clear from the UI as how you can acces his/her profile (clicking on the name) or how to chat with him/her (clicking on a message-like button). This two tasks already took me the 8-hours (which is 1 hour more than estimated) it was mainly because of the difficulty I found when implementing the service that fetches users information, since nothing really similar had been done before in our codebase. So I couldn't finish my last 1-hour task but I'll finish it next sprint.
Moreover, I recorded a video together with Aman, for showcasing the feature ask during the destabilization sprint. I hope everyone will like it as much as I would like to :) . Recording the video and video editing also took me a decent amount of extra time that I wasn't able to invest in finishing this last task.

## Thomas

This week I worked on finishing the point of interest history feature, which actually turned into a favorite poi feature. I also fixed the bug of keyboards which didn't close themselves when the user had finished creating a post. Finally, I finished Ahmed's implementation of Facebook login.

I am glad that I effectively managed to implement the Facebook login feature, especially since it was the first time that I touched that part of the codebase, and I think that I integrated it well with the existing code.

However, my time estimate for the poi history feature was not enough, as I had problems with tests that passed locally but not on cirrus, which took a lot of time to fix. But now I have learned some things which can go wrong on cirrus (such as a test failing and that making another test which doesn't fail otherwise fail) and so I will be able to be faster next week.

## Yingxuan

This week I worked on the feature that allows user to customize the app's theme. I learned how to use Preferences to remember user's choice without accessing the database, and made the current mode correspond to the checked item. I also refactor the code to avoid repetitions and changed the location of the theme button to make it reachable for all users. It took me more time than expected to match the chosen item in alert dialog with the correct theme and to reorganize the map's action bar. So I did not have time to do the other tasks.

I should be less greedy in the future and do more research before estimating the time.

## Overall team

I believe the team has overcomed the destabilization sprint exceedingly well. We have managed to implement the user story that the assistants added (calling it Meili Lens) but also we were able to implement many other cool features as improving the UI for the friends list, adding the possibility to see profiles of other users, being able to save your favorite pois and even fixing some bugs we discovered in the chat. Finally, we also started addressing the comments from the code review we received from the assistants by revising the past code but also by having them in mind while writing new code.
We are continuing to do at least 2 stand-ups per week which are proving to be really useful to share what we are doing and give our opinions on how to make the app better and how to help the other teamates. 
