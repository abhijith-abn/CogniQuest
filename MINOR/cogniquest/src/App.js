import React, { useState, useCallback, useEffect } from 'react';
import axios from 'axios';

// --- CONFIGURATION ---
const API_BASE_URL = 'http://localhost:8080/api';

// Mock Auth Credentials
const TEACHER_USER = 'thushara';
const TEACHER_PASS = 'mgthushara';

// --- API HELPER FUNCTIONS (Declared ONLY ONCE at the top) ---

const fetchCourses = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/syllabus`);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch courses:", error);
        return [];
    }
};

// --- STYLES (Inline CSS) ---
const styles = {
    container: { fontFamily: 'Arial, sans-serif', padding: '20px', maxWidth: '1200px', margin: '0 auto' },
    card: { backgroundColor: '#ffffff', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)', marginBottom: '20px' },
    headerBar: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #eee', paddingBottom: '10px', marginBottom: '20px' },
    heading: { color: '#007bff', borderBottom: '2px solid #007bff', paddingBottom: '10px', marginBottom: '20px', fontSize: '24px' },
    title: { fontSize: '28px', color: '#333', margin: '0' },
    subtitle: { fontSize: '18px', color: '#555', marginBottom: '20px' },
    button: { padding: '10px 15px', borderRadius: '5px', border: 'none', cursor: 'pointer', margin: '5px', fontWeight: 'bold', fontSize: '14px' },
    primaryButton: { backgroundColor: '#007bff', color: 'white' },
    secondaryButton: { backgroundColor: '#6c757d', color: 'white' },
    input: { padding: '10px', border: '1px solid #ccc', borderRadius: '5px', width: '100%', boxSizing: 'border-box', marginBottom: '10px' },
    textarea: { padding: '10px', border: '1px solid #ccc', borderRadius: '5px', width: '100%', minHeight: '150px', boxSizing: 'border-box' },
    error: { color: '#dc3545', backgroundColor: '#f8d7da', padding: '10px', borderRadius: '5px' },
    messageSuccess: { color: '#0f5132', backgroundColor: '#d4edda', padding: '10px', borderRadius: '5px', marginBottom: '15px' },
    loading: { color: '#ffc107', fontStyle: 'italic' },
    topicList: { listStyleType: 'decimal', paddingLeft: '20px' },
    topicItem: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px', padding: '8px 0', borderBottom: '1px dotted #eee' },
    smallButton: { padding: '5px 10px', border: 'none', color: 'white', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' },
    table: { width: '100%', borderCollapse: 'collapse', marginTop: '20px', boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)' },
    tableHeader: { backgroundColor: '#eef2ff', padding: '12px', textAlign: 'left', borderBottom: '2px solid #ddd', color: '#333' },
    tableCell: { padding: '12px', fontSize: '14px', color: '#555', borderBottom: '1px solid #eee' },
    userInfo: { display: 'flex', alignItems: 'center', gap: '15px', fontSize: '14px', color: '#333' },
    logoutButton: { padding: '8px 12px', backgroundColor: '#dc3545', color: 'white', borderRadius: '5px', cursor: 'pointer', border: 'none', fontWeight: 'bold' },
    actionButton: { padding: '8px 12px', backgroundColor: '#17a2b8', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginLeft: '5px' },
    reviewContainer: { maxHeight: '600px', overflowY: 'auto', padding: '10px', border: '1px solid #ddd', borderRadius: '5px', marginBottom: '20px' },
    tableRow: { backgroundColor: '#fff' },
    fileInput: { marginBottom: '15px', padding: '10px', border: '1px dashed #ccc', borderRadius: '5px', width: '100%', boxSizing: 'border-box' },
    questionBlock: { marginBottom: '25px', padding: '15px', border: '1px solid #eee', borderRadius: '8px', backgroundColor: '#fafafa' },
    questionText: { fontWeight: 'bold', margin: '0 0 10px 0', fontSize: '16px', color: '#333' },
    optionList: { listStyleType: 'none', paddingLeft: '0', margin: '0' },
    optionItem: { padding: '8px 12px', borderBottom: '1px solid #e0e0e0', fontSize: '14px', backgroundColor: '#fff', marginBottom: '5px', borderRadius: '4px', display: 'block' }, 
    correctAnswer: { color: '#155724', backgroundColor: '#d4edda', padding: '10px', borderRadius: '4px', marginTop: '10px', fontWeight: 'bold', border: '1px solid #c3e6cb' }
};

// --- AUTHENTICATION COMPONENTS ---

const LandingScreen = ({ onLogin }) => (
    <div style={{ ...styles.container, maxWidth: '600px', margin: '100px auto' }}>
        <h2 style={{ ...styles.heading, textAlign: 'center' }}>Welcome to CogniQuest</h2>
        <div style={{ ...styles.card, textAlign: 'center', padding: '40px' }}>
            <h3 style={{ marginBottom: '20px' }}>Select Your Role:</h3>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '20px' }}>
                <button onClick={() => onLogin({ role: 'teacher' })} style={{ ...styles.button, ...styles.primaryButton, minWidth: '150px' }}>Teacher Login</button>
                <button onClick={() => onLogin({ role: 'student' })} style={{ ...styles.button, backgroundColor: '#00cc99', color: 'white', minWidth: '150px' }}>Student Entry</button>
            </div>
        </div>
    </div>
);

const LoginScreen = ({ onLoginSuccess, role }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [rollNumber, setRollNumber] = useState('');
    const [studentPass, setStudentPass] = useState('');
    const [error, setError] = useState('');

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        if (role === 'teacher') {
            if (username === TEACHER_USER && password === TEACHER_PASS) {
                onLoginSuccess({ role: 'teacher', username: TEACHER_USER, userId: 1 });
            } else {
                setError('Invalid teacher credentials.');
            }
        } else {
            try {
                const response = await axios.post(`${API_BASE_URL}/user/auth/student`, { 
                    rollNumber: rollNumber.trim(), 
                    password: studentPass 
                });
                onLoginSuccess({ role: 'student', rollNumber: response.data.rollNumber, userId: response.data.id });
            } catch (err) {
                setError('Invalid Roll Number or Password.');
            }
        }
    };

    return (
        <div style={{ ...styles.container, maxWidth: '400px', margin: '100px auto' }}>
            <h2 style={styles.heading}>{role === 'teacher' ? 'Teacher Login' : 'Student Login'}</h2>
            <form onSubmit={handleLogin} style={styles.card}>
                {error && <p style={styles.error}>{error}</p>}
                {role === 'teacher' ? (
                    <>
                        <input type="text" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} style={styles.input} />
                        <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} style={styles.input} />
                    </>
                ) : (
                    <>
                        <input type="text" placeholder="Roll Number (e.g. S202501)" value={rollNumber} onChange={(e) => setRollNumber(e.target.value)} style={styles.input} />
                        <input type="password" placeholder="Password" value={studentPass} onChange={(e) => setStudentPass(e.target.value)} style={styles.input} />
                    </>
                )}
                <button type="submit" style={{...styles.button, ...styles.primaryButton, width: '100%'}}>Login</button>
            </form>
        </div>
    );
};

const AllocationScreen = ({ syllabus, onBack, onAllocationComplete }) => {
    const [rollNumbersText, setRollNumbersText] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [allocationSummary, setAllocationSummary] = useState(null);
    const [quizDetails, setQuizDetails] = useState(null);
    const [step, setStep] = useState(1); 

    const handleFileUpload = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (evt) => setRollNumbersText(evt.target.result);
            reader.readAsText(file);
        }
    };

    const handleAllocation = async () => {
        const lines = rollNumbersText.split(/\r?\n/).filter(line => line.trim() !== '');
        if (lines.length === 0) { setError('Please paste student data.'); return; }
        setLoading(true); setError(null);

        try {
            const response = await axios.post(`${API_BASE_URL}/user/allocate-students/${syllabus.id}`, { rollNumbers: lines });
            setAllocationSummary(response.data);
            setLoading(false);
            setStep(2); 

        } catch (err) {
            setLoading(false);
            setError(err.response?.data?.message || "Allocation failed.");
        }
    };
    
    const handleGenerateQuizAccess = async () => {
        setLoading(true); setError(null);
        
        const examPassword = Math.random().toString(36).substring(2, 8).toUpperCase(); 
        const examId = syllabus.courseName.replace(/\s/g, '').toUpperCase().substring(0, 5) + Math.floor(Math.random() * 10000);

        try {
            const quizResponse = await axios.post(`${API_BASE_URL}/quiz/create-quiz`, { syllabusId: syllabus.id, examId, examPassword });
            setQuizDetails(quizResponse.data);
            setLoading(false);
        } catch (error) {
            setLoading(false);
            setError(`Quiz creation failed: ${error.response?.data?.message || 'Internal error.'}`);
        }
    };

    const handleFinish = () => {
        onAllocationComplete(syllabus.id);
    };

    return (
        <div style={styles.card}>
            <h1 style={styles.heading}>Manage Access: {syllabus.courseName}</h1>
            
            {error && <p style={styles.error}>{error}</p>}

            {step === 1 && (
                <>
                    <p style={styles.subtitle}><strong>Step 1: Allocate Students.</strong> Upload CSV or paste list (Format: <code>RollNumber, Password</code>). Roll Numbers without passwords will default to 'student123'.</p>
                    <input type="file" accept=".csv,.txt" onChange={handleFileUpload} style={styles.fileInput} />
                    <textarea value={rollNumbersText} onChange={(e) => setRollNumbersText(e.target.value)} style={styles.textarea} placeholder="S202501, pass123&#10;S202502, secure456" rows="10" disabled={loading} />
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
                        <button onClick={onBack} style={{ ...styles.button, ...styles.secondaryButton }}>&larr; Back to Dashboard</button>
                        <button onClick={handleAllocation} disabled={loading || rollNumbersText.trim() === ''} style={{ ...styles.button, backgroundColor: '#4CAF50' }}>
                            {loading ? 'Processing Allocation...' : '1. Save Students & Continue'}
                        </button>
                    </div>
                </>
            )}

            {step === 2 && (
                <div style={{ marginTop: '20px' }}>
                    <p style={styles.messageSuccess}>Allocation successful! <strong>{allocationSummary?.totalAllocated || 0}</strong> students processed.</p> 
                    
                    {!quizDetails ? (
                        <button onClick={handleGenerateQuizAccess} disabled={loading} style={{ ...styles.button, backgroundColor: loading ? '#ccc' : '#007bff', marginTop: '20px' }}>{loading ? 'Generating...' : '2. GENERATE QUIZ ACCESS CODE'}</button>
                    ) : (
                        <div style={{ ...styles.card, marginTop: '20px', border: '1px solid #007bff' }}>
                            <h3 style={{ color: '#007bff' }}>Quiz Live!</h3>
                            <p><strong>Course:</strong> {syllabus.courseName}</p>
                            <p><strong>Quiz ID:</strong> <code>{quizDetails.examId}</code></p>
                            <p><strong>Password:</strong> <code>{quizDetails.examPassword}</code></p>
                            <p style={{ color: '#dc3545', fontWeight: 'bold', marginTop: '10px' }}>Students can now log in.</p>
                            
                            {/* FINAL FINISH BUTTON - Triggers Refresh */}
                            <button onClick={handleFinish} style={{ ...styles.button, ...styles.secondaryButton, marginTop: '20px', width: '100%' }}>Finish & Return to Dashboard</button>
                        </div>
                    )}
                </div>
            )}
            
            {/* Display Back button when the screen is first loaded (before step 1 or error in step 1) */}
            {step === 1 && (
                <div style={{ display: 'flex', justifyContent: 'flex-start', marginTop: '20px' }}>
                    <button onClick={onBack} style={{ ...styles.button, ...styles.secondaryButton }}>&larr; Back to Dashboard</button>
                </div>
            )}
        </div>
    );
};


// Helper component for editing topics inline
const EditableTopicItem = ({ topic, index, onUpdate, onRemove, isFinalized, onGenerateTopicQs, generatingQuestions, onReviewQs }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [name, setName] = useState(topic.name);
    
    // Determine the status of the question bank for styling
    const questionsExist = topic.questions && topic.questions.length > 0;
    
    // Fix for "Generating..." showing on all buttons: check if this specific topic is generating
    const isGeneratingThisTopic = generatingQuestions && generatingQuestions[topic.id];

    const itemStyle = {
        ...styles.topicItem, 
        backgroundColor: questionsExist ? '#e6ffe6' : 'white', 
        borderLeft: questionsExist ? '5px solid #4CAF50' : '5px solid #ddd'
    };


    const handleSave = () => {
        if (name.trim() !== topic.name) onUpdate(topic.id || index, name);
        setIsEditing(false);
    };

    return (
        <li key={topic.id || `new-${index}`} style={itemStyle}>
            <div style={{ flexGrow: 1 }}>
                {isEditing ? (
                    <input type="text" value={name} onChange={(e) => setName(e.target.value)} onBlur={handleSave} onKeyDown={(e) => { if (e.key === 'Enter') handleSave(); }} style={styles.input} autoFocus />
                ) : (
                    <span>{index + 1}. <strong>{topic.name}</strong></span>
                )}
            </div>
            
            <div style={{ display: 'flex', gap: '8px' }}>
                
                {/* Review Qs Button (If questions exist) */}
                {isFinalized && questionsExist && 
                    <button 
                        onClick={() => onReviewQs(topic)} // Pass the entire topic object for review
                        style={{ ...styles.smallButton, backgroundColor: '#007bff' }}
                    >
                        Review Qs ({topic.questions.length})
                    </button>
                }
                
                {/* Generate Qs Button (Module 2) */}
                {isFinalized && !questionsExist && (
                    <button
                        onClick={() => onGenerateTopicQs(topic.id)}
                        disabled={isGeneratingThisTopic}
                        style={{ ...styles.smallButton, backgroundColor: isGeneratingThisTopic ? '#9c27b0' : '#8A2BE2' }}
                        title="Generate 30 questions for this single topic"
                    >
                        {isGeneratingThisTopic ? 'Generating...' : 'Generate Qs'}
                    </button>
                )}

                {/* Edit/Remove Buttons (Module 1) */}
                {!isFinalized && (
                    <>
                        <button
                            onClick={() => setIsEditing(!isEditing)}
                            style={{ ...styles.smallButton, backgroundColor: isEditing ? '#ffc107' : '#2196F3', marginRight: '8px' }}
                        >
                            {isEditing ? 'Save' : 'Edit'}
                        </button>
                        <button
                            onClick={() => onRemove(topic.id || index)}
                            style={{ ...styles.smallButton, backgroundColor: '#f44336' }}
                        >
                            Remove
                        </button>
                    </>
                )}
            </div>
        </li>
    );
};


// --- QUESTION REVIEW SCREEN ---

const QuestionReviewScreen = ({ topic, questions, onBack, onConfirmQuestions }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');
    const [showSample, setShowSample] = useState(false); // State to toggle sample view

    // Toggle logic for 10-Q sample quiz
    const initialQuestions = questions;
    const displayedQuestions = showSample 
        ? [
            ...initialQuestions.filter(q => q.difficulty === 'EASY').slice(0, 4),
            ...initialQuestions.filter(q => q.difficulty === 'INTERMEDIATE').slice(0, 3),
            ...initialQuestions.filter(q => q.difficulty === 'ADVANCED').slice(0, 3)
          ]
        : initialQuestions;

    const groupedQuestions = displayedQuestions.reduce((acc, q) => {
        const diff = q.difficulty || 'UNKNOWN';
        if (!acc[diff]) acc[diff] = [];
        acc[diff].push(q);
        return acc;
    }, {});
    const difficultyOrder = ['EASY', 'INTERMEDIATE', 'ADVANCED', 'UNKNOWN'];

    const handleConfirm = async () => {
        setLoading(true); setError(null); setMessage('Saving questions...');
        try {
            // CRITICAL: We save the full, unfiltered 'questions' array (all 30)
            const response = await axios.put(`${API_BASE_URL}/question/confirm-and-save/${topic.id}`, { questions: questions });
            setMessage(`SUCCESS: Saved all ${response.data.length} questions.`);
            onConfirmQuestions(topic.id); // Navigate back after save
        } catch (err) { setError("Failed to save questions."); } finally { setLoading(false); }
    };

    return (
        <div style={styles.card}>
            <h1 style={styles.heading}>Review Questions: {topic.name}</h1>
            <p style={styles.subtitle}>Total Bank: <strong>{initialQuestions.length}</strong> | Viewing: <strong>{displayedQuestions.length}</strong></p>
            
            {message && !error && <p style={styles.messageSuccess}>{message}</p>}
            {error && <p style={styles.error}>{error}</p>}
            
            <div style={{marginBottom: '15px', padding:'10px', backgroundColor:'#f8f9fa', borderRadius:'5px', display:'flex', alignItems:'center'}}>
                <span style={{marginRight: '10px', fontWeight:'bold'}}>Actions:</span>
                <button 
                    onClick={() => setShowSample(!showSample)} 
                    style={{...styles.button, backgroundColor: showSample ? '#6f42c1' : '#6c757d', padding:'8px 12px'}}
                >
                    {showSample ? "Show Full Bank (30)" : "Generate 10-Q Quiz"} 
                </button>
                <span style={{fontSize:'12px', marginLeft:'10px', color:'#666'}}>
                    {showSample ? "(Previewing 4E+3I+3A set)" : "(Viewing all generated questions)"}
                </span>
            </div>

            <div style={styles.reviewContainer}>
                {Object.entries(groupedQuestions).sort(([d1], [d2]) => difficultyOrder.indexOf(d1) - difficultyOrder.indexOf(d2)).map(([difficulty, qList]) => (
                    <div key={difficulty} style={{ marginBottom: '25px' }}>
                        <h3 style={{ color: difficulty === 'EASY' ? '#4CAF50' : difficulty === 'INTERMEDIATE' ? '#FF9800' : '#f44336', borderBottom: '1px solid #ccc', paddingBottom:'5px' }}>{difficulty} ({qList.length})</h3>
                        <div style={{ paddingLeft: '10px' }}>
                            {qList.map((q, i) => {
                                let options = [];
                                try {
                                    options = JSON.parse(q.optionsJson);
                                } catch (e) {
                                    options = [String(q.optionsJson)];
                                }

                                return (
                                    <div key={q.id || i} style={styles.questionBlock}>
                                        <p style={styles.questionText}>{i+1}. {q.questionText}</p>
                                        <ul style={styles.optionList}>
                                            {Array.isArray(options) ? options.map((opt, idx) => (
                                                <li key={idx} style={styles.optionItem}>{opt}</li>
                                            )) : <li style={styles.optionItem}>{options}</li>}
                                        </ul>
                                        <div style={styles.correctAnswer}>Correct Answer: {q.correctAnswerText}</div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px', borderTop: '1px solid #eee', paddingTop: '20px' }}>
                <button onClick={onBack} style={{ ...styles.button, ...styles.secondaryButton }} disabled={loading}>&larr; Back to Topics</button>
                <button onClick={handleConfirm} disabled={loading} style={{ ...styles.button, backgroundColor: '#4CAF50' }}>{loading ? 'Saving...' : 'Confirm & Save FULL Bank (30 Qs)'}</button>
            </div>
        </div>
    );
};

const TopicReviewScreen = ({ syllabus, onBack, onFinalize, onRegenerate, onUpdateCourseData, onStartAllocation }) => {
    const [localTopics, setLocalTopics] = useState(syllabus.topics);
    const [viewQuestions, setViewQuestions] = useState(null); 
    const [generatingQuestions, setGeneratingQuestions] = useState({}); 
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => { setLocalTopics(syllabus.topics || []); }, [syllabus]);

    const handleTopicUpdate = (id, name) => setLocalTopics(prev => prev.map(t => (t.id === id ? { ...t, name } : t)));
    const handleTopicRemove = (id) => setLocalTopics(prev => prev.filter(t => t.id !== id));
    
    const handleSaveAndFinalize = async () => {
        if (localTopics.length === 0) return;
        setLoading(true);
        try {
            const cleanTopics = localTopics.filter(t => t.name && t.name.trim() !== '');
            const response = await axios.put(`${API_BASE_URL}/syllabus/finalize-topics/${syllabus.id}`, { topics: cleanTopics });
            onFinalize(response.data);
        } catch(err) { setError("Finalization failed."); } finally { setLoading(false); }
    };

    const handleGenerateTopicQs = async (topicId) => {
        setGeneratingQuestions(prev => ({ ...prev, [topicId]: true }));
        try {
            const response = await axios.post(`${API_BASE_URL}/question/generate`, { topicId });
            setGeneratingQuestions(prev => ({ ...prev, [topicId]: false }));
            const topicToReview = localTopics.find(t => t.id === topicId);
            setViewQuestions({ topic: topicToReview, questions: response.data });
        } catch (err) { setError("Generation Failed"); }
        setGeneratingQuestions(prev => ({ ...prev, [topicId]: false }));
    };
    
    const handleQuestionsConfirmed = () => {
        setViewQuestions(null);
        onUpdateCourseData(false);
    };

    if (viewQuestions) return <QuestionReviewScreen topic={viewQuestions.topic} questions={viewQuestions.questions} onBack={() => setViewQuestions(null)} onConfirmQuestions={handleQuestionsConfirmed} />;
    
    const isFinalized = syllabus.isApproved;

    return (
        <div style={styles.card}>
            <h1 style={styles.heading}>Topic Review: {syllabus.courseName}</h1>
            {error && <p style={styles.error}>{error}</p>}
            <p style={{...styles.card, backgroundColor: isFinalized ? '#d4edda' : '#fff3cd', padding: '10px'}}><strong>Status:</strong> {syllabus.isApproved ? 'Finalized' : 'Pending'}</p>
            <div style={styles.reviewContainer}>
                <ol style={styles.topicList}>
                    {localTopics.map((t, i) => (
                        <EditableTopicItem
                            key={t.id || i}
                            topic={t}
                            index={i}
                            onUpdate={handleTopicUpdate}
                            onRemove={handleTopicRemove}
                            isFinalized={isFinalized}
                            onGenerateTopicQs={handleGenerateTopicQs}
                            generatingQuestions={generatingQuestions}
                            onReviewQs={(topic) => setViewQuestions({ topic: topic, questions: topic.questions })}
                        />
                    ))}
                </ol>
            </div>
            <div style={{ ...styles.headerBar, borderBottom: 'none' }}>
                <button onClick={onBack} style={{ ...styles.button, ...styles.secondaryButton }}>&larr; Dashboard</button>
                {!isFinalized && <button onClick={handleSaveAndFinalize} disabled={loading} style={{ ...styles.button, backgroundColor: '#4CAF50' }}>Finalize & Save</button>}
                {isFinalized && (
                    <button onClick={() => onStartAllocation(syllabus.id)} style={{ ...styles.button, backgroundColor: '#007bff', marginLeft: 'auto' }}>
                        PROCEED TO STUDENT ALLOCATION
                    </button>
                )}
            </div>
        </div>
    );
};

const CourseCreationScreen = ({ onTopicsGenerated, onBack }) => {
    const [courseName, setCourseName] = useState('');
    const [syllabusText, setSyllabusText] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleGenerateTopics = async () => {
        if (!courseName.trim() || !syllabusText.trim()) { setError("All fields required."); return; }
        setError(''); setIsLoading(true);
        try {
            const response = await axios.post(`${API_BASE_URL}/syllabus/generate-topics`, { courseName, syllabusText });
            onTopicsGenerated(response.data);
        } catch (err) {
            setError("Topic generation failed.");
        } finally { setIsLoading(false); }
    };

    return (
        <div style={styles.container}>
            <div style={styles.headerBar}><h1 style={styles.title}>New Subject</h1><button onClick={onBack}>Back</button></div>
            <div style={styles.card}>
                {error && <p style={styles.error}>{error}</p>}
                <input type="text" placeholder="Course Name" value={courseName} onChange={(e) => setCourseName(e.target.value)} style={styles.input} />
                <textarea placeholder="Syllabus" value={syllabusText} onChange={(e) => setSyllabusText(e.target.value)} style={styles.textarea} rows="15" />
                <button onClick={handleGenerateTopics} disabled={isLoading} style={styles.button}>{isLoading ? 'Processing...' : 'Generate'}</button>
            </div>
        </div>
    );
};

const TeacherDashboard = ({ user, onLogout }) => {
    const [view, setView] = useState('list'); 
    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState(null);

    const loadCourses = useCallback(async () => {
        const fetchedCourses = await fetchCourses();
        setCourses(fetchedCourses);
    }, []);

    useEffect(() => { loadCourses(); }, [loadCourses]);

    const handleAllocateClick = (course) => { setSelectedCourse(course); setView('allocate'); };
    const handleReviewClick = (course) => { setSelectedCourse(course); setView('review'); };
    const handleCreateClick = () => { setView('create'); };

    if (view === 'create') return <CourseCreationScreen onTopicsGenerated={(c) => { setSelectedCourse(c); setView('review'); }} onBack={() => setView('list')} />;
    if (view === 'review') return <TopicReviewScreen syllabus={selectedCourse} onBack={() => { setView('list'); loadCourses(); }} onFinalize={(c) => { setSelectedCourse(c); loadCourses(); }} onRegenerate={() => alert("Regen logic here")} onUpdateCourseData={loadCourses} onStartAllocation={(id) => { setSelectedCourse(courses.find(c => c.id === id)); setView('allocate'); }} />;
    
    // Updated: Passing onAllocationComplete to trigger reload
    if (view === 'allocate') return (
        <AllocationScreen 
            syllabus={selectedCourse} 
            onBack={() => { setView('list'); loadCourses(); }} 
            onAllocationComplete={(id) => { 
                setView('list'); 
                loadCourses(); // FORCE REFRESH HERE
            }} 
        />
    );

    return (
        <div style={styles.container}>
            <div style={styles.headerBar}><h1 style={styles.title}>Teacher Dashboard</h1><div style={styles.userInfo}><span>Logged in as: <strong>{user.username}</strong></span><button onClick={onLogout} style={styles.logoutButton}>Logout</button></div></div>
            <button onClick={handleCreateClick} style={styles.button}>+ Create New Subject</button>
            <h2 style={styles.subtitle}>My Subjects</h2>
            <table style={styles.table}>
                <thead><tr><th style={styles.tableHeader}>Course Name</th><th style={styles.tableHeader}>Status</th><th style={styles.tableHeader}>Students</th><th style={styles.tableHeader}>Actions</th></tr></thead>
                <tbody>
                    {courses.map(c => (
                        <tr key={c.id}>
                            <td style={styles.tableCell}>{c.courseName}</td>
                            <td style={styles.tableCell}>{c.isApproved ? <span style={{color:'green'}}>Active</span> : <span style={{color:'orange'}}>Draft</span>}</td>
                            <td style={styles.tableCell}><strong>{c.allocatedStudentCount || 0}</strong></td>
                            <td style={styles.tableCell}>
                                <button onClick={() => handleAllocateClick(c)} style={{...styles.actionButton, backgroundColor: '#17a2b8'}}>Allocate Students</button>
                                <button onClick={() => handleReviewClick(c)} style={{...styles.actionButton, ...styles.primaryButton}}>Review/Generate Qs</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

const StudentDashboard = ({ user, onLogout }) => {
    return <div style={styles.container}><h1>Student Dashboard (Coming Soon)</h1><button onClick={onLogout}>Logout</button></div>;
};

const App = () => {
    const [user, setUser] = useState(null);
    const [role, setRole] = useState(null);
    if (!role) return <LandingScreen onLogin={(r) => setRole(r.role)} />;
    if (!user) return <LoginScreen role={role} onLoginSuccess={setUser} />;
    if (role === 'teacher') return <TeacherDashboard user={user} onLogout={() => { setUser(null); setRole(null); }} />;
    return <StudentDashboard user={user} onLogout={() => { setUser(null); setRole(null); }} />;
};

export default App;