package com.example.mqttkotlinsample

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import org.eclipse.paho.client.mqttv3.*

class ClientFragment : Fragment() {
    private lateinit var mqttClient : MQTTClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mqttClient.isConnected()) {
                    // Disconnect from MQTT Broker
                    mqttClient.disconnect(object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(this.javaClass.name, "Disconnected")

                            Toast.makeText(context, "MQTT Disconnection success", Toast.LENGTH_SHORT).show()

                            // Disconnection success, come back to Connect Fragment
                            findNavController().navigate(R.id.action_ClientFragment_to_ConnectFragment)
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d(this.javaClass.name, "Failed to disconnect")
                        }
                    })
                } else {
                    Log.d(this.javaClass.name, "Impossible to disconnect, no server connected")
                }
            }
        })
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments passed by ConnectFragment
        val serverURI   = arguments?.getString(MQTT_SERVER_URI_KEY)
        val clientId    = arguments?.getString(MQTT_CLIENT_ID_KEY)
        val username    = arguments?.getString(MQTT_USERNAME_KEY)
        val pwd         = arguments?.getString(MQTT_PWD_KEY)

        var valor1: Double = 0.0

        // Check if passed arguments are valid
        if (    serverURI   != null    &&
                clientId    != null    &&
                username    != null    &&
                pwd         != null        ) {
            // Open MQTT Broker communication
            mqttClient = MQTTClient(context, serverURI, clientId)

            // Connect and login to MQTT Broker
            mqttClient.connect( username,
                    pwd,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(this.javaClass.name, "Connection success")

                            Toast.makeText(context, "MQTT Connection success", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")

                            Toast.makeText(context, "MQTT Connection fails: ${exception.toString()}", Toast.LENGTH_SHORT).show()

                            // Come back to Connect Fragment
                            findNavController().navigate(R.id.action_ClientFragment_to_ConnectFragment)
                        }
                    },
                    object : MqttCallback {
                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            val msg = "Receive message: ${message.toString()} from topic: $topic"
                            Log.d(this.javaClass.name, msg)

                            if(valor1 == 0.0){
                                valor1 = message.toString().toDouble()
                            } else {
                                var valor2 = message.toString().toDouble()
                                var v = (valor2-valor1)/3

                                if(v>80.0) {
                                    Toast.makeText(context, "Multado. Velocidade: $v", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Velocidade Permitida. Velocidade: $v", Toast.LENGTH_SHORT).show()
                                }
                                valor1 = 0.0
                            }

                            // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }

                        override fun connectionLost(cause: Throwable?) {
                            Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
                        }

                        override fun deliveryComplete(token: IMqttDeliveryToken?) {
                            Log.d(this.javaClass.name, "Delivery complete")
                        }
                    })
        } else {
            // Arguments are not valid, come back to Connect Fragment
            findNavController().navigate(R.id.action_ClientFragment_to_ConnectFragment)
        }

        view.findViewById<Button>(R.id.button_disconnect).setOnClickListener {
            if (mqttClient.isConnected()) {
                // Disconnect from MQTT Broker
                mqttClient.disconnect(object : IMqttActionListener {
                                            override fun onSuccess(asyncActionToken: IMqttToken?) {
                                                Log.d(this.javaClass.name, "Disconnected")

                                                Toast.makeText(context, "MQTT Disconnection success", Toast.LENGTH_SHORT).show()

                                                // Disconnection success, come back to Connect Fragment
                                                findNavController().navigate(R.id.action_ClientFragment_to_ConnectFragment)
                                            }

                                            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                                                Log.d(this.javaClass.name, "Failed to disconnect")
                                            }
                                        })
            } else {
                Log.d(this.javaClass.name, "Impossible to disconnect, no server connected")
            }
        }

        view.findViewById<Button>(R.id.button_subscribe).setOnClickListener {
            val topic   = view.findViewById<EditText>(R.id.edittext_subtopic).text.toString()

            if (mqttClient.isConnected()) {
                mqttClient.subscribe(topic,
                        1,
                        object : IMqttActionListener {
                            override fun onSuccess(asyncActionToken: IMqttToken?) {
                                val msg = "Subscribed to: $topic"
                                Log.d(this.javaClass.name, msg)

                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }

                            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                                Log.d(this.javaClass.name, "Failed to subscribe: $topic")
                            }
                        })
            } else {
                Log.d(this.javaClass.name, "Impossible to subscribe, no server connected")
            }
        }

        view.findViewById<Button>(R.id.button_unsubscribe).setOnClickListener {
            val topic   = view.findViewById<EditText>(R.id.edittext_subtopic).text.toString()

            if (mqttClient.isConnected()) {
                mqttClient.unsubscribe( topic,
                        object : IMqttActionListener {
                            override fun onSuccess(asyncActionToken: IMqttToken?) {
                                val msg = "Unsubscribed to: $topic"
                                Log.d(this.javaClass.name, msg)

                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }

                            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                                Log.d(this.javaClass.name, "Failed to unsubscribe: $topic")
                            }
                        })
            } else {
                Log.d(this.javaClass.name, "Impossible to unsubscribe, no server connected")
            }
        }
    }
}