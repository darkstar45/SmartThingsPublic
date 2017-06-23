/**
 *  Hue B Smart Lux Bulb
 *
 *  Copyright 2016 Anthony Pastor
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
 *	Version 1.0b - fixed icon to single
 * 
 *	Version 1.1 - Conformed DTH
 * 
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue B Smart Lux Bulb", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
       
        command "reset"
        command "refresh"
        command "updateStatus"
        command "flash"
        command "flash_off"
		command "ttUp"
        command "ttDown"
        command "setTransitionTime"
        command "sendToHub"
        command "setLevel"
       
		attribute "transitionTime", "NUMBER"
		attribute "bri", "number"
        attribute "level", "number"
        attribute "on", "string"
        attribute "reachable", "string"
        attribute "hueID", "string"
        attribute "host", "string"
        attribute "username", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#C6C7CC", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#C6C7CC", nextState:"turningOn"
			}                       
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }			
		}

		/* reset / refresh */	
		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"Reset", action:"reset", icon:"st.lights.philips.hue-multi"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        /* Flash / Alert */
		standardTile("flash", "device.flash", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"Flash", action:"flash", icon:"st.lights.philips.hue-multi"
		}
        
                
        /* transition time */
		valueTile("ttlabel", "transitionTime", decoration: "flat", width: 2, height: 1) {
			state "default", label:'Transition: ${currentValue}00ms     '
		}
		standardTile("ttdown", "device.transitionTime", decoration: "flat", width: 2, height: 1) {
			state "default", label: "Transition -", action:"ttDown"
		}
		standardTile("ttup", "device.transitionTime", decoration: "flat", width: 2, height: 1) {
			state "default", label:"Transition +", action:"ttUp"
		}
        
        /* misc */
        valueTile("hueID", "device.hueID", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: 'BulbID: ${currentValue}'
		}    
        
        valueTile("host", "device.host", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", label: 'Host: ${currentValue}'
        }
        valueTile("username", "device.username", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", label: 'User: ${currentValue}'
        }

	}
	main(["rich-control"])
	details(["rich-control","hueID","ttlabel","ttup","ttdown","flash","reset","refresh"]) //  "host", "username", 
}


private configure() {		
    def commandData = parent.getCommandData(device.deviceNetworkId)
    log.debug "${commandData = commandData}"
    sendEvent(name: "hueID", value: commandData.deviceId, displayed:true, isStateChange: true)
    sendEvent(name: "host", value: commandData.ip, displayed:false, isStateChange: true)
    sendEvent(name: "username", value: commandData.username, displayed:false, isStateChange: true)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}


def ttUp() {
	log.trace "Hue B Smart Lux Bulb: ttUp(): "
    def tt = this.device.currentValue("transitionTime") ?: 0
    if (tt == null) { tt = 4 }
    sendEvent(name: "transitionTime", value: tt + 1)
}

def ttDown() {
	log.trace "Hue B Smart Lux Bulb: ttDown(): "
	def tt = this.device.currentValue("transitionTime") ?: 0
    tt = tt - 1
    if (tt < 0) { tt = 0 }
    sendEvent(name: "transitionTime", value: tt)
}

/** 
 * capability.switchLevel 
 **/
def setLevel(inLevel) {
	log.trace "Hue B Smart Lux Bulb: setLevel ( ${inLevel} ): "
	def level = parent.scaleLevel(inLevel, true, 254)
    def commandData = parent.getCommandData(device.deviceNetworkId)    
    def tt = this.device.currentValue("transitionTime") ?: 0
    
	parent.sendHubCommand(new physicalgraph.device.HubAction(
    	[
        	method: "PUT",
			path: "/api/${commandData.username}/lights/${commandData.deviceId}/state",
	        headers: [
	        	host: "${commandData.ip}"
			],
	        body: [on: true, bri: level, transitiontime: tt]
		])
	)    
}


/** 
 * capability.switch
 **/
def on() {
	log.trace "Hue B Smart Lux Bulb: on(): "

    def commandData = parent.getCommandData(device.deviceNetworkId)    
	def tt = device.currentValue("transitionTime") as Integer ?: 0
    def percent = device.currentValue("level") as Integer ?: 100
    def level = parent.scaleLevel(percent, true, 254)
    
        return new physicalgraph.device.HubAction(
    	[
        	method: "PUT",
			path: "/api/${commandData.username}/lights/${commandData.deviceId}/state",
	        headers: [
	        	host: "${commandData.ip}"
			],
	        body: [on: true, bri: level, transitiontime: tt]
		])
}

def off() {
	log.trace "Hue B Smart Lux Bulb: off(): "
    
    def commandData = parent.getCommandData(device.deviceNetworkId)
    def tt = device.currentValue("transitionTime") as Integer ?: 0
    
    //parent.sendHubCommand(
    return new physicalgraph.device.HubAction(
    	[
        	method: "PUT",
			path: "/api/${commandData.username}/lights/${commandData.deviceId}/state",
	        headers: [
	        	host: "${commandData.ip}"
			],
	        body: [on: false, transitiontime: tt]
		])
//	)
}

/** 
 * capability.polling
 **/
def poll() {
	log.trace "Hue B Smart Lux Bulb: poll(): "
    refresh()
}

/**
 * capability.refresh
 **/
def refresh() {
	log.trace "Hue B Smart Lux Bulb: refresh(): "
    parent.doDeviceSync()
    configure()
}

def reset() {
	log.trace "Hue B Smart Lux Bulb: reset(): "

	def value = [level:100, saturation:56, hue:23]
    sendToHub(value)
}

/**
 * capability.alert (flash)
 **/

def flash() {
	log.trace "Hue B Smart Lux Bulb: flash(): "
    def commandData = parent.getCommandData(device.deviceNetworkId)
	parent.sendHubCommand(new physicalgraph.device.HubAction(
    	[
        	method: "PUT",
			path: "/api/${commandData.username}/lights/${commandData.deviceId}/state",
	        headers: [
	        	host: "${commandData.ip}"
			],
	        body: [alert: "lselect"]
		])
	)
    
    runIn(5, flash_off)
}

def flash_off() {
	log.trace "Hue B Smart Lux Bulb: flash_off(): "
    
    def commandData = parent.getCommandData(device.deviceNetworkId)
	parent.sendHubCommand(new physicalgraph.device.HubAction(
    	[
        	method: "PUT",
			path: "/api/${commandData.username}/lights/${commandData.deviceId}/state",
	        headers: [
	        	host: "${commandData.ip}"
			],
	        body: [alert: "none"]
		])
	)
}

                
/**
 * Update Status
 **/
private updateStatus(action, param, val) {
	log.trace "Hue B Smart Lux Bulb: updateStatus ( ${param}:${val} )"
	if (action == "state") {
		switch(param) {
        	case "on":
            	def onoff
            	if (val == true) {
                	sendEvent(name: "switch", value: on, displayed:false, isStateChange: true)                	     
                
                } else {
	            	sendEvent(name: "switch", value: off, displayed:false)
                	sendEvent(name: "alert", value: "none", displayed:false, isStateChange: true)    
                }    
                break
            case "bri":
            	sendEvent(name: "level", value: parent.scaleLevel(val), displayed:false, isStateChange: true) 
                break
            case "transitiontime":
            	sendEvent(name: "transitionTime", value: val, displayed:false, isStateChange: true)
                break                
            case "alert":
            	if (val == "none") {
            		flash_off() 
                } else {
                	flash()
                }
                break
                
			case "reachable":
				sendEvent(name: "reachable", value: val, displayed:false, isStateChange: true)
				break
			default: 
				log.debug("Unhandled parameter: ${param}. Value: ${val}")    
        }
    }
}


def getDeviceType() { return "lux bulb" }