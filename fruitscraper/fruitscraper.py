from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
import csv
from selenium.webdriver.chrome.options import Options
from datetime import date
import json
import time

#Function that scrapes coles fruit prices
def coles_scrape():
    
    #Opens the coles csv and reads results
    with open('coles.csv', newline='') as f:
        reader = csv.reader(f)
        data = list(reader)
    
    #options for chrome webdriver
    chrome_options = Options()
    #chrome_options.add_argument("--disable-extensions")
    #chrome_options.add_argument("--disable-gpu")
    #chrome_options.add_argument("--no-sandbox") # linux only
    #chrome_options.add_argument("--headless")

    #Enter Webdriver path
    s=Service('/Users/matthewfuaux/Documents/fruitscraper/chromedriver')
    driver = webdriver.Chrome(service=s, options=chrome_options)
    today = date.today()
    mylist = []
    for entry in data:
        driver.get(str(entry[2]))
        time.sleep(2)
    
        search = driver.find_elements(by=By.CLASS_NAME, value="package-price")
        text = search[0].text
        type = ""
        if "per 1Kg" in text:
            text = text.replace(" per 1Kg","")
            type="per kg"
        else:
            text = text.replace(" per 1Ea","")
            type="each"
        nameString = '"name" : "'+ entry[1]+'",'
        priceString = '"price" : "' + text + '",'
        quantityString = '"quantity" : "'+ type +'",'
        storeString = '"store" : "coles",'
        dateString = '"date" : "' + str(today) +'"'

        jsonObject = '"'+entry[0]+'" : [{' + nameString + priceString + quantityString + storeString + dateString + '}],'

        mylist.append(jsonObject)
    return mylist
    

def main():
    list = coles_scrape()
    jsonString = '{'
    for x in list:
        jsonString = jsonString + x
    jsonString = jsonString[:-1]
    jsonString = jsonString + '}'
   

    f = open("fruitPrices.json", "a")
    f.write(jsonString)
    f.close()

if __name__ == '__main__':
    main()
