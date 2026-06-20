import { useState } from 'react';
import ResumeAnalysisContext from './ResumeAnalysisContext';

const ResumeAnalysisProvider = ({ children }) => {
  const uploadedResume = null;
  const jobDescription = null;
  const analysisResult = null;
  const loading = null;
  const error = null;

  return (
    <ResumeAnalysisContext.Provider
      value={{
        uploadedResume,
        jobDescription,
        analysisResult,
        loading,
        error,
      }}
    >
      {children}
    </ResumeAnalysisContext.Provider>
  );
};

export default ResumeAnalysisProvider;
