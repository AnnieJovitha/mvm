MVM ~ Mobile Vaadin Maps
========================

This is a simple demo and test application, built after some "can I do this or that with TouchKit/Vaadin" questions. This is not an example on how you should build productions applications. For example there is no persistency at all, data is stored in memory only.

To build this project you'll need to have snapshot builds of TouchKit 2 and
OpenLayers Wrapper. Should work with release versions of them as well, but I fixed some issues in both while building this example. So if you want to play with this project do svn co and mvn install stuff in these projects:

http://dev.vaadin.com/svn/branches/TouchKit2/vaadin-touchkit-agpl/

http://vopenlayers.googlecode.com/svn/trunk/

A version of this is deployed to my budget server at, but don't expect it to be fast or always up:
http://v3.tahvonen.fi/mvm/

Tricks that this project demonstrates
-------------------------------------

 * "How do I create application with remember me functionality?"
  * Check MobileVaadinMaps class. Implementation is bit more complex than usually needed, as it is tuned for ios home screen web apps and application cache.
 * "Can I draw points and lines on a map in Vaadin?"
  * Of course you can! See MainView and updateMyRoute() function. This app uses OpenStreetMap background map and optionally some terrain maps in Finland from national mapping agency, Maanmittauslaitos. OpenLayers supports huge amount of different layer types so almost anything is possible.
 * "Why don't TouchKit have all those fancy animations that are in competitor X?"
  * It is feature of "HTML5", not TouchKit. This example app makes settings panel visible with "flip" effect. In Vaadin apps this kind of stuff eye candy belongs to themes. Both map and settings view are actually all the time rendered, but only one of them is displayed at once with some lines of CSS magic. Changing the view use css3 transitions to make the flip effect. Works very smoothly on modern iOS and Android devices. Check styles.css file and MainView.
 * "How to access device Camera? Do I need to use PhoneGap?"
  * Don't use PhoneGap. Modern iOS and Android devices work perfectly without it if it is just camera you need. Use Upload component. In this example I'm using EasyUploads to make things even simpler. Check PlaceMarkEditor.
 * "How do I make iPhone 5 use whole screen estate in 'home screen' mode?"
  * Apple decided to letterbox home screen web apps that use 'width:device-width' in viewport settings. Don't know the reasoning behind this, but most likely not all web apps would scale as well as TouchKit based stuff. To use all the estate, you need to remove some setting we currently use as defaults. See init method in MobileVaadinMaps for correct combo. If these settings don't brake too much, I'll make these new defaults.
 * "What's up with geolocation in iOS 6?"
  * It is broken. Works randomly very little. In browser bit better, in home screen mode it practically does not work. That is a very sad regression and I have no workaround to present. Unless one of following will do
   * Use Android 4.2 and powerful device
   * Don't upgrade to iOS 6, iOS 5 is just fine and geolocation works like a charm.
   * Join Apple iOS developer program to upgrade your device to iOS 6.1 which fixes the issue.
 * "Is there an icon library included in TouchKit?"
  * No. But there are lots of good free icon sets in the interwebs. Unicode symbols is one easy hack, also used partially in this app. Note that e.g. Android don't have all symbols I have used in this example.
   