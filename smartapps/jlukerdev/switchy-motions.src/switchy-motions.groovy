/**
 *  SwitchyMotions
 *
 *  Copyright 2017 Josh Luker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Switchy.Motions",
    namespace: "jlukerdev",
    author: "Josh Luker",
    description: "Switchy.Motions parent app",
    singleInstance: true,
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@2x.png")


preferences {
	section("Title") {
		app(name: "childApps", appName: "Switchy.Motion", namespace: "jlukerdev", title: "New SwitchyMotion action", multiple: true)
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers