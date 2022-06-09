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
    chrome_options.add_argument("--headless")

    #Enter Webdriver path
    s=Service('/home/ubuntu/team-12-project/scraper/chromedriver')
    driver = webdriver.Chrome(service=s, options=chrome_options)
    today = date.today()
    mylist = []
    for entry in data:
        print("Scraping: " + str(entry[0]) + " - " + str(entry[1]))
        driver.get(str(entry[2]))
        time.sleep(2)
    
        search = driver.find_elements(by=By.CLASS_NAME, value="package-price")
        text = search[0].text
        type = ""
        if "per 1Kg" in text:
            text = text.replace(" per 1Kg","")
            text = text.replace("$","")
            type="per kg"
        else:
            text = text.replace(" per 1Ea","")
            text = text.replace("$","")
            type="each"
        indexString = '"index" : "' + entry[0]+'",'
        nameString = '"name" : "'+ entry[1]+'",'
        priceString = '"price" : "' + text + '",'
        quantityString = '"quantity" : "'+ type +'",'
        storeString = '"store" : "coles",'
        dateString = '"date" : "' + str(today) +'"'

        jsonObject = '{' + indexString + nameString + priceString + quantityString + storeString + dateString + '},'
        print("Scraped price: " + str(entry[0]) + " - " + str(entry[1]) + " " + priceString)
        mylist.append(jsonObject)
    return mylist
    

def main():
    list = coles_scrape()
    jsonString = '{ "colesFruitPrices" : [ '
    for x in list:
        jsonString = jsonString + x
    jsonString = jsonString[:-1]
    jsonString = jsonString + ']}'
   

    f = open("fruitPrices.json", "a")
    f.write(jsonString)
    f.close()
    print("Scraping complete")

if __name__ == '__main__':
    main()
