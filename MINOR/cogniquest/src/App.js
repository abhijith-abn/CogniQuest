// src/App.js
import React, { useState } from 'react';

const App = () => {
  const [syllabusText, setSyllabusText] = useState('');
  const [extractedTopics, setExtractedTopics] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSyllabusChange = (event) => {
    setSyllabusText(event.target.value);
  };

  const handleSubmit = async () => {
    setIsLoading(true);
    setError(null);
    setExtractedTopics([]);

    try {
      const response = await fetch('http://localhost:8080/api/syllabus/process', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ syllabusText }),
      });

      if (!response.ok) {
        throw new Error('Failed to process syllabus. Please try again.');
      }

      const data = await response.json();
      setExtractedTopics(data.topics);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const mainContainerStyle = {
    minHeight: '100vh',
    backgroundColor: '#f3f4f6',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '1rem',
  };

  const cardStyle = {
    backgroundColor: '#ffffff',
    padding: '2rem',
    borderRadius: '1rem',
    boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
    width: '100%',
    maxWidth: '42rem',
  };

  const titleStyle = {
    fontSize: '1.875rem',
    fontWeight: '700',
    color: '#1f2937',
    marginBottom: '1.5rem',
    textAlign: 'center',
  };

  const paragraphStyle = {
    color: '#4b5563',
    marginBottom: '2rem',
    textAlign: 'center',
  };

  const textareaStyle = {
    width: '100%',
    height: '12rem',
    padding: '1rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.75rem',
    outline: 'none',
    transition: 'all 0.2s ease-in-out',
    // Focus styles are a limitation of inline styles.
    // In a real app, you would use a stylesheet for this.
  };

  const buttonStyle = {
    width: '100%',
    marginTop: '1rem',
    padding: '0.75rem 1.5rem',
    backgroundColor: isLoading || syllabusText.trim() === '' ? '#a5b4fc' : '#4f46e5',
    color: '#ffffff',
    fontWeight: '600',
    borderRadius: '0.75rem',
    cursor: isLoading || syllabusText.trim() === '' ? 'not-allowed' : 'pointer',
    transition: 'background-color 0.2s ease-in-out',
    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
  };
  
  const errorStyle = {
    marginTop: '1rem',
    padding: '1rem',
    backgroundColor: '#fee2e2',
    color: '#dc2626',
    borderRadius: '0.75rem',
    border: '1px solid #fecaca',
  };

  const topicsContainerStyle = {
    marginTop: '2rem',
  };

  const topicsTitleStyle = {
    fontSize: '1.5rem',
    fontWeight: '700',
    color: '#1f2937',
    marginBottom: '1rem',
  };

  const topicsListStyle = {
    listStyle: 'none',
    padding: 0,
    marginTop: '0.75rem',
  };

  const topicItemStyle = {
    backgroundColor: '#f9fafb',
    padding: '1rem',
    borderRadius: '0.75rem',
    boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    border: '1px solid #e5e7eb',
    marginBottom: '0.75rem',
  };

  const topicNameStyle = {
    fontSize: '1.125rem',
    fontWeight: '500',
    color: '#374151',
  };

  const topicsFooterStyle = {
    marginTop: '1rem',
    fontSize: '0.875rem',
    color: '#6b7280',
  };

  return (
    <div style={mainContainerStyle}>
      <div style={cardStyle}>
        <h1 style={titleStyle}>
          CogniQuest: Syllabus to Topics
        </h1>
        <p style={paragraphStyle}>
          Paste your course syllabus below, and our AI will extract key concepts for your quizzes.
        </p>

        <textarea
          style={textareaStyle}
          placeholder="Paste your syllabus here..."
          value={syllabusText}
          onChange={handleSyllabusChange}
          disabled={isLoading}
        ></textarea>

        <button
          onClick={handleSubmit}
          style={buttonStyle}
          disabled={isLoading || syllabusText.trim() === ''}
        >
          {isLoading ? 'Processing...' : 'Extract Concepts'}
        </button>

        {error && (
          <div style={errorStyle}>
            {error}
          </div>
        )}

        {extractedTopics.length > 0 && (
          <div style={topicsContainerStyle}>
            <h2 style={topicsTitleStyle}>Extracted Topics</h2>
            <ul style={topicsListStyle}>
              {extractedTopics.map((topic, index) => (
                <li key={index} style={topicItemStyle}>
                  <p style={topicNameStyle}>
                    {topic.name}
                  </p>
                </li>
              ))}
            </ul>
            <p style={topicsFooterStyle}>
              Review and approve these topics to generate questions in the next step.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default App;
