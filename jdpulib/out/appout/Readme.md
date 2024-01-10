files acquire:

> https://github.com/daveshap/PlainTextWikipedia




```
git clone https://github.com/daveshap/PlainTextWikipedia.git
```
```
cd PlainTextWikipedia
```
modify the dewiki_functions.py file

```python
from threading import Thread
import json
import re
from html2text import html2text as htt
import wikitextparser as wtp

def dewiki(text):
    text = wtp.parse(text).plain_text()  # wiki to plaintext 
    text = htt(text)  # remove any HTML
    text = text.replace('\\n',' ')  # replace newlines
    text = re.sub('\s+', ' ', text)  # replace excess whitespace
    return text


def analyze_chunk(text):
    try:
        if '<redirect title="' in text:  # this is not the main article
            return None, None, None
        if '(disambiguation)' in text:  # this is not an article
            return None, None, None
        else:
            title = text.split('<title>')[1].split('</title>')[0]
            title = htt(title)
            if ':' in title:  # most articles with : in them are not articles we care about
                return None, None, None
        serial = text.split('<id>')[1].split('</id>')[0]
        content = text.split('</text')[0].split('<text')[1].split('>', maxsplit=1)[1]
        content = dewiki(content)
        return title, content.strip(), serial
    except Exception as oops:
        print(oops)
        return None, None, None


def save_article(article, savedir):
    title, doc, docid = analyze_chunk(article)
    if doc:
        print('SAVING:', title)

        filename = str(docid) + '.txt'
        with open(savedir + filename, 'w', encoding='utf-8') as outfile:
            outfile.write(doc)


def process_file_text(filename, savedir):
    article = ''
    with open(filename, 'r', encoding='utf-8') as infile:
        for line in infile:
            if '<page>' in line:
                article = ''
            elif '</page>' in line:  # end of article
                Thread(target=save_article, args=(article, savedir)).start()
            else:
                article += line 
```

```
wget https://dumps.wikimedia.org/simplewiki/latest/simplewiki-latest-pages-articles-multistream.xml.bz2
bzip2 -d simplewiki-latest-pages-articles-multistream.xml.bz2 
```

modify `wiki_to_text.py`
```python
from dewiki_functions import *

wiki_xml_file = './simplewiki-latest-pages-articles-multistream.xml'  # update this
json_save_dir = './wiki_plaintext/'

if __name__ == '__main__':
    process_file_text(wiki_xml_file, json_save_dir)

```


```
mkdir wiki_plaintext
python wiki_to_text.py
```

