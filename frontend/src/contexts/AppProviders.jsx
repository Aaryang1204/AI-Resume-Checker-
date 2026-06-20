import AuthProvider from './AuthProvider';
import ResumeAnalysisProvider from './ResumeAnalysisProvider';

const AppProviders = ({ children }) => {
  return (
    <AuthProvider>
      <ResumeAnalysisProvider>{children}</ResumeAnalysisProvider>
    </AuthProvider>
  );
};

export default AppProviders;
