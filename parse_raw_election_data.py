import json

with open('election-county-2012.json', 'r') as f:
    results = json.load(f)

final = {}
countyArray = []
for result in results:
    county = result['county-name'] + ' County'
    countyForList = result['county-name'] + ' County' + ', ' + result['state-postal']
    countyArray.append(countyForList)
    key = '%s, %s' % (county, result['state-postal'])
    final[key] = {
        'obama': result['obama-percentage'],
        'romney': result['romney-percentage']
    }

with open('election_results_2012.json', 'w') as f:
    json.dump(final, f)
f.close()

print("done with json dump")

with open('list_of_counties.txt', 'w') as f:
    for county in countyArray:
        f.write(county + "\n")
f.close()

print("done with county list")
