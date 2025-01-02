from flask import Flask, request, jsonify
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

app = Flask(__name__)

# Initialize VADER Sentiment Analyzer
analyzer = SentimentIntensityAnalyzer()

@app.route('/analyze_sentiment', methods=['POST'])
def analyze_sentiment():
    data = request.get_json()
    text = data.get('text', '')

    # Analyze sentiment
    sentiment_score = analyzer.polarity_scores(text)

    # Return sentiment result
    return jsonify(sentiment_score)

def run():
    app.run(debug=True)

