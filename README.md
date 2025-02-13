![](https://github.com/hackvertor/hackvertor/blob/master/src/main/resources/images/logo-light.png)

# Hackvertor

Hackvertor is a tag based conversion tool written in Java implemented as a Burp Suite extension. Tags are constructed as follows:
`<@base64></@base64>` the @ symbol is used as an identifier that it's a Hackvertor tag followed by the name of the tag in this case base64.

Tags also support arguments. The find tag allows you to find a string by regex and has parenthesis after the tag name:
`<@find("\\w")>abc</@find>` this indicates it supports arguments. The argument in this case is the regex string to find on the text in-between the tags. Hackvertor allows you to use three types of arguments either strings (double, single), boolean (true, false) or numbers (including hex).

# Changelog

**2.0.12 2025-02-13**

- Added AI features!
  - Learn from repeater! Hackvertor will attempt to learn the encoding from repeater requests and generate Python custom tags automatically.
  - Summarise custom code tags. When a custom tag is created Hackvertor will use AI to summarise what it does.
  - AI custom tags. You can now use prompts in custom tags.
  - Use AI to generate code. Hackvertor will generate a custom tag for you if you give it some input/output and instructions

**2.0.0 2025-01-08**

- Added tag execution key rehydrate button
- Made HTTP request editor more compact
- Added smart decode feature
- Changed style of tags to `</@name>` instead `<@/name>` to help with autocompletion in future

**1.8.10 2024-01-08**

- Added new line and space tags
- Added ean13 tag to the tag store
- Allowed regex replace to use capture groups

**1.8.9 2023-12-22**

- Fixed #79 No contextual menu entries for requests in Proxy History and Sitemap 

**1.8.8  2023-12-20**

- Added remove output tag
- Added load from json file
- Added save to json file

**1.8.6  2023-12-20**

- Added line numbers to custom tag editor

**1.8.6  2023-12-19**

- Added full support for JavaScript in custom tags

**1.8.5  2023-12-18**

- Fixed bug where hex default value for custom tag would be quoted

**1.8.4  2023-11-1**

- Continued improvements on create tag window.

**1.8.3  2023-11-1**

- Disabled install button when tag is installed
- Started work on create new tag to make more room

**1.8.2  2023-10-31**
- Fixed editing tags without producing duplicates
- Added export to tag store

**1.8.1  2023-10-30**
- Fixed bug when installing a tag from the tag store with the same name.

**1.8  2023-10-26**
- Implemented tag store. Installable tags from Github.

# Installation

- In order to use Hackvertor you need to open Burp Suite.
- Click the Extender tab
- Click the BApp store tab inside the Extender tab
- Scroll down and click Hackvertor
- Then click install on the right

# How to use Hackvertor

To use Hackvertor once it has been installed, click on the Hackvertor tab in the main Burp Suite window. You can then type into the input box to create some text to convert. For instance if you want to convert some text to base64, select the text in the input box then click on the encode tab in Hackvertor, then find the base64 tag and click it. Hackvertor will then add the tag around the selected text and the output window will show a base64 encoded string of your text. It's worth noting that Hackvertor supports an unlimited amount of nesting, you can use multiple tags to encode or decode text. Hackvertor will work from the inner most tag to the outer tag and each step will be converted using the relevant tag you have chosen.

# Advanced usage

For more advanced users, you can use tags within repeater tabs. Simply click the repeater tab, right click and select the Hackvertor menu. Then you can use any tag within the repeater tab. Tags will be displayed in the repeater window but when a request is sent they will be converted by Hackvertor and the server will see the converted request. Hackvertor also have a message editor tab, you can select this tab from any request tab in Burp. This will then create the Hackvertor interface inside a request tab, allowing to use the Hackvertor interface to modify a request. 
