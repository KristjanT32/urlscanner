# URL Checker

<hr>
## What's this program?
  Ever gotten those pesky screamers shoved right up your [[BEAUTIFUL FACE]]?
  Well, jokes aside, this program <b>may</b> help you.
  This program will scan any valid URL you give it and
  show you the possible risks of actually entering the website.
  You can, of course specify <i>almost</i> everything to check for.
<hr>
## How does it work?
<hr>
  So, let me explain what it actually does.
  This program is no wizard, but has show it to be quite effective.
<br>
  Firstly, the program checks the HTML code for the indications of
  videos, images, links and <iframe>'s.
  (You can specify whether videos/images SHOULD NOT be present on the
  website.)
  By specifying that, you tell the program all of the possible red flags.
  By checking all of the elements, it determines whether it'd be a good
  idea to visit this website.
<br>
<hr>
**Hardcoded checked elements**
<hr>
*These are the elements that the program checks*
  * a-tags that contain a href
  * img-tags
  * iframe-tags
  * video-tags
    * video-tags that have the "loop" property
