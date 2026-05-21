lines = ""

for line in input.replace("(", "\\(").split("\n"):
  lines += "("+line+")'\n"

output = """%PDF-1.1
% a (really) generic PDF with page + text, Ange Albertini, bsd licence 2012
1 0 obj
<<
%	/Type /Catalog
	/Pages 2 0 R
>>
endobj

2 0 obj
<<
	/Type /Pages
	/Count 1
	/Kids [ 3 0 R ]
>>
endobj

3 0 obj
<<
	/Type /Page
	/Contents 4 0 R
	/Parent 2 0 R
	/Resources <<
		/Font <<
			/F1 <<
				/Type /Font
				/Subtype /Type1
				/BaseFont /Arial
			>>
		>>
	>>
>>
endobj

4 0 obj
<< >>
stream
BT
/F1 20
Tf 30 750 Td 20 TL
"""+lines+"""
ET
endstream
endobj

trailer
<<
	/Root 1 0 R
>>"""