import zipfile
import StringIO
import time

# Turn a CSV string into a valid XLSX string
def csv_to_xlsx(csv_content):
    # 1. Generate the Sheet XML
    lines = csv_content.strip().split('\n')
    sheet_xml = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    sheet_xml += '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">'
    sheet_xml += '<sheetData>'
    for r, line in enumerate(lines):
        sheet_xml += '<row r="%d">' % (r + 1)
        # Handle basic comma split (ignoring quoted commas for brevity)
        for c, val in enumerate(line.split(',')):
            col_letter = chr(65 + c)
            sheet_xml += '<c r="%s%d" t="inlineStr"><is><t>%s</t></is></c>' % (col_letter, r+1, val.strip())
        sheet_xml += '</row>'
    sheet_xml += '</sheetData></worksheet>'

    # 2. Minimal Workbook XML
    workbook_xml = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    workbook_xml += '<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">'
    workbook_xml += '<sheets><sheet name="Sheet1" sheetId="1" r:id="rId1"/></sheets></workbook>'

    # 3. Relationships (.rels)
    rel_workbook = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    rel_workbook += '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    rel_workbook += '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>'
    rel_workbook += '</Relationships>'

    rel_root = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    rel_root += '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    rel_root += '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>'
    rel_root += '</Relationships>'

    # 4. Content Types
    content_types = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    content_types += '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
    content_types += '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
    content_types += '<Default Extension="xml" ContentType="application/xml"/>'
    content_types += '<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>'
    content_types += '<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
    content_types += '</Types>'

    # 5. Zip it all together
    output = StringIO.StringIO()
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as zx:
        zx.writestr('[Content_Types].xml', content_types)
        zx.writestr('_rels/.rels', rel_root)
        zx.writestr('xl/workbook.xml', workbook_xml)
        zx.writestr('xl/_rels/workbook.xml.rels', rel_workbook)
        zx.writestr('xl/worksheets/sheet1.xml', sheet_xml)

    return output.getvalue()

output = csv_to_xlsx(input)
