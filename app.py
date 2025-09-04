from flask import Flask, request, jsonify
from flask_cors import CORS
import dns.message
import dns.query
import dns.rcode
import socket

app = Flask(__name__)
CORS(app)

@app.route('/resolve', methods=['POST'])
def resolve_domain():
    data = request.json
    domain_name = data.get('domain')
    
    if not domain_name:
        return jsonify({"error": "No domain provided"}), 400

    try:
        # Create a DNS query message
        query = dns.message.make_query(domain_name, 'A')
        
        # Send the query to your local Java DNS server
        response = dns.query.udp(query, '127.0.0.1', port=53, timeout=5)
        
        # Check the DNS response code
        if response.rcode() == dns.rcode.NOERROR:
            # Domain resolved successfully
            if response.answer:
                # Extract the IP address from the response
                ip_address = response.answer[0][0].address
                return jsonify({"domain": domain_name, "ip_address": ip_address})
            else:
                return jsonify({"domain": domain_name, "error": "No IP address found in response."})
        elif response.rcode() == dns.rcode.NXDOMAIN:
            return jsonify({"domain": domain_name, "error": "Domain not found (NXDOMAIN)."})
        else:
            return jsonify({"domain": domain_name, "error": "Unknown DNS error."})
    
    except dns.exception.Timeout:
        return jsonify({"error": "DNS query timed out."}), 504
    except socket.error as e:
        return jsonify({"error": f"Socket error: {e}"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)