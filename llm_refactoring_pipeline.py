from abc import ABC, abstractmethod
import google.generativeai as genai
from llama_cpp import Llama
from typing import List, Tuple, Dict
import os
import argparse
from github import Github
from datetime import datetime
from pathlib import Path
import shutil
from git import Repo, InvalidGitRepositoryError, GitCommandError
import requests
from dotenv import load_dotenv
import json
import openai
from groq import Groq

# import replicate


class BaseLLMHandler(ABC):
    """Abstract base class for LLM handlers"""

    @abstractmethod
    def analyze_code_for_smells(self, files: List[Tuple[str, str]]) -> Dict:
        pass

    @abstractmethod
    def generate_refactored_code(self, files: str, analysis: str) -> str:
        pass

    def _prepare_analysis_prompt(self, files: List[Tuple[str, str]]) -> str:
        """Common prompt preparation for analysis"""
        prompt = "Analyze the following files for code smells and design smells, focusing on architectural and design issues across files:\n\n"
        for filename, content in files:
            prompt += f"File: {filename}\n```\n{content}\n```\n\n"
        prompt += "\nIdentify design smells and code smells, and provide a detailed explanation of the issues found. Provide the names of the design smells and where these errors are occurring (line numbers) and their explanations, one per line. Do not include any additional formatting or details. Just return in text format."
        
        return prompt


class GeminiHandler(BaseLLMHandler):
    """Handles communication with Google's Gemini models"""

    def __init__(self, api_key: str, model: str = "gemini-pro"):
        self.model = model
        genai.configure(api_key=api_key)
        self.model_instance = genai.GenerativeModel(model)

    def analyze_code_for_smells(self, files: List[Tuple[str, str]]) -> str:
        context = self._prepare_analysis_prompt(files)
        try:
            response = self.model_instance.generate_content(
                contents=[
                    {
                        "role": "user",
                        "parts": [
                            {
                                "text": "You are a code review expert. List all the design smells present in the provided files. Only return the names of the design smells and where these errors are occuring (line numbers), one per line. Do not include JSON formatting, or additional details."
                            }
                        ],
                    },
                    {"role": "user", "parts": [{"text": context}]},
                ]
            )
            return response.text
        except Exception as e:
            raise Exception(f"Gemini Analysis failed: {str(e)}")

    def generate_refactored_code(self, file_path: str, analysis: str) -> str:
        """
        Generates refactored code based on the file path and analysis provided.

        :param file_path: Path to the Java file that needs refactooring.
        :param analysis: A string containing explanations of design smells found in the file.
        :return: The refactored code as a string.
        """

        print("Processing refactoring...")
        print("File Path:", file_path)
        print("Analysis:", analysis)

        # Directly constructing the refactoring prompt
        prompt = (
            f"You are a code refactoring expert. Given the following Java file at '{file_path}' and the detected design smells: \n\n"
            f"{analysis}\n\n"
            "Provide the complete refactored file content. Ensure the code is properly formatted and maintains functionality. "
            "Do not include explanations, JSON formatting, or extra text—only return the full refactored code. "
            "Only give the complete refactored code for the file."
        )

        # Generate refactored code using the model instance
        response = self.model_instance.generate_content(
            contents=[
                {"role": "user", "parts": [{"text": prompt}]},
            ]
        )

        print("Refactored Code Response:", response.text)
        response_lines = response.text.split("\n")
        cleaned_response = "\n".join(response_lines[1:-1])
        return cleaned_response


class QwenHandler(BaseLLMHandler):
    """Handles communication with Qwen's code models"""

    def __init__(self, api_key: str, model: str = "Qwen-coder-33b-instruct"):
        """
        Initialize the Qwen handler.

        Args:
            api_key (str): The API key for Qwen
            model (str): The model to use (default: Qwen-coder-33b-instruct)
        """
        self.api_key = api_key
        self.model = model
        self.client = Groq(api_key=api_key)

    def analyze_code_for_smells(self, files: List[Tuple[str, str]]) -> str:
        """
        Analyzes code for design smells using Qwen's code model.

        Args:
            files: List of tuples containing (file_path, content)
        Returns:
            str: Analysis of design smells found
        """
        # Prepare the context with all files
        context = self._prepare_analysis_prompt(files)

        try:
            response = self.client.chat.completions.create(
                messages=[
                    {
                        "role": "system",
                        "content": "You are a code review expert specialized in identifying design smells in Java code.",
                    },
                    {
                        "role": "user",
                        "content": "List all the design smells present in the provided files. Only return the names of the design smells and where these errors are occurring (line numbers), one per line. Do not include any additional formatting or details.",
                    },
                    {"role": "user", "content": context},
                ],
                model="qwen-2.5-32b"
            )

            return response.choices[0].message.content
        except Exception as e:
            raise Exception(f"Qwen Analysis failed: {str(e)}")

    def generate_refactored_code(self, file_path: str, analysis: str) -> str:
        """
        Generates refactored code based on the file path and analysis provided.

        Args:
            file_path: Path to the Java file that needs refactoring
            analysis: String containing explanations of design smells found in the file
        Returns:
            str: The refactored code
        """
        print("Processing refactoring...")
        print("File Path:", file_path)
        print("Analysis:", analysis)

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": "You are a code refactoring expert specialized in improving Java code quality while maintaining functionality.",
                    },
                    {
                        "role": "user",
                        "content": f"""Given the following Java file at '{file_path}' and the detected design smells:

                        {analysis}

                        Provide the complete refactored file content. Ensure the code is properly formatted and maintains functionality.
                        Only return the full refactored code for the file, without any explanations or additional text.""",
                    },
                ],
                temperature=0.2,  # Lower temperature for more consistent refactoring
                max_tokens=4000,  # Increased token limit for larger files
            )

            refactored_code = response.choices[0].message.content

            # Clean up the response by removing any potential markdown code blocks
            if refactored_code.startswith("```java"):
                refactored_code = refactored_code.split("```java")[1]
            if refactored_code.startswith("```"):
                refactored_code = refactored_code.split("```")[1]
            if refactored_code.endswith("```"):
                refactored_code = refactored_code.rsplit("```", 1)[0]

            return refactored_code.strip()

        except Exception as e:
            raise Exception(f"Qwen Refactoring failed: {str(e)}")

    def _prepare_analysis_prompt(self, files: List[Tuple[str, str]]) -> str:
        """
        Prepares the analysis prompt from the list of files.

        Args:
            files: List of tuples containing (file_path, content)
        Returns:
            str: Formatted prompt containing all files
        """
        context = "Review the following Java files for design smells:\n\n"
        for file_path, content in files:
            context += f"File: {file_path}\n\n```java\n{content}\n```\n\n"
        return context


class LlamaHandler(BaseLLMHandler):
    """Handles communication with Llama models via Groq"""

    def __init__(self, api_key: str, model: str = "mixtral-8x7b-32768"):
        """
        Initialize the Llama handler using Groq's API.

        Args:
            api_key (str): The Groq API key
            model (str): The model to use (default: mixtral-8x7b-32768)
        """
        self.api_key = api_key
        self.model = model
        self.client = Groq(api_key=api_key)

    def analyze_code_for_smells(self, files: List[Tuple[str, str]]) -> str:
        """
        Analyzes code for design smells using Groq's API.

        Args:
            files: List of tuples containing (file_path, content)
        Returns:
            str: Analysis of design smells found
        """
        context = self._prepare_analysis_prompt(files)

        try:
            completion = self.client.chat.completions.create(
                messages=[
                    {
                        "role": "system",
                        "content": "You are a code review expert specializing in identifying design smells in Java code.",
                    },
                    {
                        "role": "user",
                        "content": "List all the design smells present in the provided files. Only return the names of the design smells and where these errors are occurring (line numbers), one per line. Do not include any additional formatting or details.",
                    },
                    {"role": "user", "content": context},
                ],
                model="llama3-70b-8192",
                # temperature=0.3,
                # max_tokens=1000
            )

            return completion.choices[0].message.content.strip()

        except Exception as e:
            raise Exception(f"Code Analysis failed: {str(e)}")

    def generate_refactored_code(self, file_path: str, analysis: str) -> str:
        """
        Generates refactored code using Groq's API.

        Args:
            file_path: Path to the Java file that needs refactoring
            analysis: String containing explanations of design smells found in the file
        Returns:
            str: The refactored code
        """
        print("Processing refactoring...")
        print("File Path:", file_path)
        print("Analysis:", analysis)

        try:
            completion = self.client.chat.completions.create(
                messages=[
                    {
                        "role": "system",
                        "content": "You are a code refactoring expert specializing in improving Java code quality while maintaining functionality.",
                    },
                    {
                        "role": "user",
                        "content": f"""Given the following Java file at '{file_path}' and the detected design smells:

                            {analysis}

                            Provide the complete refactored file content. Ensure the code is properly formatted and maintains functionality.
                            Only return the full refactored code for the entire file, without any explanations or additional text.""",
                    },
                ],
                model="llama3-70b-8192",
                # temperature=0.2,
                # max_tokens=4000
            )

            refactored_code = completion.choices[0].message.content.strip()

            # print("l309 Refactored Code:", refactored_code)

            # Clean up any potential markdown formatting
            if refactored_code.startswith("```java"):
                refactored_code = refactored_code.split("```java")[1]
            if refactored_code.startswith("```"):
                refactored_code = refactored_code.split("```")[1]
            if refactored_code.endswith("```"):
                refactored_code = refactored_code.rsplit("```", 1)[0]

            return refactored_code.strip()

        except Exception as e:
            raise Exception(f"Code Refactoring failed: {str(e)}")

    def _prepare_analysis_prompt(self, files: List[Tuple[str, str]]) -> str:
        """
        Prepares the analysis prompt from the list of files.

        Args:
            files: List of tuples containing (file_path, content)
        Returns:
            str: Formatted prompt containing all files
        """
        context = "Review the following Java files for design smells:\n\n"
        for file_path, content in files:
            context += f"File: {file_path}\n\n```java\n{content}\n```\n\n"
        return context


class RefactoringPipeline:
    """Main class orchestrating the refactoring pipeline"""

    def __init__(
        self,
        github_token: str,
        llm_handler: BaseLLMHandler,
        repo_url: str,
        local_path: str = "./temp",
    ):
        self.github_token = github_token
        self.github = Github(github_token)
        self.llm_handler = llm_handler
        self.repo_url = repo_url.replace(
            "https://", f"https://x-access-token:{github_token}@"
        )
        self.local_path = local_path
        self.repo_owner = repo_url.split("/")[0]
        self.repo_name = repo_url.split("/")[-1]
        self.github_repo = self.github.get_repo(self.repo_url)

        if not self.repo_url.startswith("http"):
            self.repo_url = f"https://github.com/{self.repo_url}.git"

        print("l179 self.repo_name = ", self.repo_name)
        print("l180 self.repo_url = ", self.repo_url)
        print("l181 self.github_token = ", self.github_token)

    def clone_or_pull_repo(self) -> None:
        """Clone the repository if it doesn't exist locally, or pull latest changes if it does"""

        if os.path.exists(self.local_path):
            try:
                repo = Repo(self.local_path)
                if not repo.bare:  # If it's a valid Git repo
                    print("Repository already cloned. Pulling latest changes...")
                    origin = repo.remotes.origin
                    origin.pull()

                    # Try switching to 'main' branch, fallback to 'master'
                    try:
                        repo.git.checkout("main")
                    except GitCommandError:
                        repo.git.checkout("master")

                    self.repo = repo
                    return  # Exit function if pull is successful
                else:
                    raise InvalidGitRepositoryError
            except (InvalidGitRepositoryError, GitCommandError):
                print("Invalid or corrupted repository. Removing and re-cloning...")
                shutil.rmtree(self.local_path)  # Remove bad repo

        # If repo doesn't exist or was deleted, clone it fresh
        print("Cloning repository...")
        repo = Repo.clone_from(self.repo_url, self.local_path)

        # Try switching to 'main' branch, fallback to 'master'
        try:
            repo.git.checkout("main")
        except GitCommandError:
            repo.git.checkout("master")

        self.repo = repo

    def list_java_files(self) -> List[str]:
        """List all Java files in the repository"""
        java_files = []
        for root, _, files in os.walk(self.local_path):
            for file in files:
                if file.endswith(".java"):
                    java_files.append(os.path.join(root, file))
        if len(java_files) > 2:
            java_files = java_files[:2]
        return java_files

    def create_and_checkout_branch(self, branch_name: str) -> bool:
        """Create and checkout a new branch"""
        try:
            self.repo.git.checkout(b=branch_name)
            print(f"Branch '{branch_name}' created and checked out successfully!")
            return True
        except Exception as e:
            print(f"Failed to create and check out branch. Error: {e}")
            return False

    def create_pull_request(self, branch_name: str, analysis: Dict) -> None:
        """Create a pull request with the refactored code"""
        try:
            pr_title = f"Refactor code at {int(datetime.now().timestamp())}"
            pr_body = self._generate_pr_description(analysis)

            # Ensure the branch exists remotely
            self.repo.git.push("--set-upstream", "origin", branch_name)

            # Get default branch safely
            base_branch = self.github_repo.default_branch
            print(f"Default branch: {base_branch}")  # Debugging

            print("l256 self.repo_name = ", self.repo_name)
            print("l257 self.repo_url = ", self.repo_url)
            print("l258 self.github_repo = ", self.github_repo)

            # Corrected GitHub API URL to create a pull request
            url = f"https://api.github.com/repos/{self.github_repo.owner.login}/{self.repo_name}/pulls"

            print("l270 url = ", url)

            # Debugging token being used
            if not self.github_token:
                print("Error: GitHub token is missing!")
                return

            # Prepare the request headers with the token
            headers = {
                "Authorization": f"Bearer {self.github_token}",  # Use the stored token here
                "Accept": "application/vnd.github.v3+json",
            }

            # Prepare the payload with the PR details
            payload = {
                "title": pr_title,
                "body": pr_body,
                "head": branch_name,
                "base": base_branch,
                "draft": False,  # Set to True if the PR should be a draft
            }

            # Send the POST request to create the pull request
            response = requests.post(url, json=payload, headers=headers)

            # Handle the response
            if response.status_code == 201:
                pr = response.json()
                print(f"Pull Request Created: {pr['html_url']}")
            else:
                print(
                    f"Failed to create pull request. Status code: {response.status_code}"
                )
                print(response.text)
                return None

        except Exception as e:
            raise Exception(f"Failed to create pull request: {str(e)}")

    def _generate_pr_description(self, analysis: Dict) -> str:
        """Generate detailed pull request description"""
        description = "The code has been refactored based on smells detected.\n\n"
        for file_path, smells in analysis.items():
            description += f"Design smells in {file_path}\n{smells}\n\n"
        return description

    def run_pipeline(self) -> None:
        """Execute the complete refactoring pipeline"""
        try:
            # Clone or pull repository
            self.clone_or_pull_repo()
            print("l274 Repository cloned/pulled successfully")

            # Get Java files
            java_files = self.list_java_files()
            print(f"l278 Found {len(java_files)} Java files")

            # Create new branch
            timestamp = int(datetime.now().timestamp())
            branch_name = f"branch_{timestamp}"
            if not self.create_and_checkout_branch(branch_name):
                raise Exception("Failed to create branch")

            analysis = {}

            for i, file_path in enumerate(java_files):
                print(f"l289 Analyzing code in file: {file_path}")

                with open(file_path, "r") as file:
                    code = file.read()[
                        :3000
                    ]  # Limiting to first 3000 characters of the file content

                # Pass file content as a tuple (filename, content)
                files_for_analysis = [(file_path, code)]

                # Analyze code using LLM
                code_smells = self.llm_handler.analyze_code_for_smells(
                    files_for_analysis
                )

                print("l356 code_smells = ", code_smells)

                # Update analysis with the code smells
                analysis[file_path] = code_smells

                # Generate and write refactored code
                refactored_code = self.llm_handler.generate_refactored_code(
                    file_path, code_smells
                )
                print("l402")

                with open(file_path, "w", encoding="utf-8") as file:
                    file.write(refactored_code)

                print(f"Refactored code saved to {file_path}")

                # if i == 0:  # Break after the first file to preserve API limit
                #     break

            # Commit changes
            print("l308")
            self.repo.git.add("--all")
            self.repo.index.commit(f"Refactor some code at {timestamp}")

            # Create pull request
            print("l313")
            self.create_pull_request(branch_name, analysis)
            print("Pipeline completed successfully")

        except Exception as e:
            raise Exception(f"Pipeline execution failed: {str(e)}")


def get_llm_handler(llm_type: str, **kwargs) -> BaseLLMHandler:
    """Factory function to create appropriate LLM handler"""
    if llm_type.lower() == "gemini":
        return GeminiHandler(
            api_key=kwargs.get("api_key"), model=kwargs.get("model", "gemini-pro")
        )
    elif llm_type.lower() == "qwen":
        return QwenHandler(
            api_key=kwargs.get("api_key"),
            model=kwargs.get("model", "hardik_kalia"),
        )
    elif llm_type.lower() == "llama":
        return LlamaHandler(
            api_key=kwargs.get("api_key"), model=kwargs.get("model", "llama-2-70b-chat")
        )
    else:
        raise ValueError(f"Unsupported LLM type: {llm_type}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="LLM Refactoring Pipeline")
    parser.add_argument(
        "--llm",
        type=str,
        default="llama",
        help="Type of LLM to use (e.g., llama, gpt)",
    )

    # Get llm type
    args = parser.parse_args()
    llm_type = args.llm.lower()

    # Configuration
    load_dotenv()
    github_token = os.getenv("GITHUB_TOKEN")
    gemini_api_key = os.getenv("GEMINI_API_KEY")
    llama_api_key = os.getenv("GROQ_API_KEY")

    repo_name = "AdityaMishraOG/java_repo_for_testing_se_project1"

    if llm_type == "gemini":
        llm_handler = get_llm_handler(
            "gemini", api_key=os.getenv("GEMINI_API_KEY"), model="gemini-pro"
        )
    elif llm_type == "qwen":
        llm_handler = get_llm_handler(
            "qwen",
            api_key=os.getenv("GROQ_API_KEY"),
            model="hardik_kalia",
        )
    elif llm_type == "llama":
        llm_handler = get_llm_handler(
            "llama", api_key=os.getenv("GROQ_API_KEY"), model="llama-2-70b-chat"
        )
    else:
        raise ValueError(f"Unsupported LLM type: {llm_type}")

    # Create and run pipeline
    pipeline = RefactoringPipeline(github_token, llm_handler, repo_name)
    pipeline.run_pipeline()
