package com.example.util

import com.example.data.model.Certificate
import com.example.data.model.CertificateCategories

object SampleData {
    fun getStarterCertificates(userId: String): List<Certificate> {
        return listOf(
            Certificate(
                userId = userId,
                title = "AWS Certified Solutions Architect - Associate",
                organization = "Amazon Web Services (AWS)",
                category = CertificateCategories.CERTIFICATION,
                issueDate = "2024-04-18",
                expiryDate = "2027-04-18",
                credentialId = "AWS-SAA-8849201",
                credentialUrl = "https://aws.amazon.com/verification",
                description = "Validated comprehensive expertise in distributed application architecture, high-availability VPC networks, and serverless computing on AWS cloud infrastructure.",
                skills = listOf("Cloud Architecture", "AWS", "Docker", "DevOps", "Security"),
                gradeOrScore = "Score: 890 / 1000"
            ),
            Certificate(
                userId = userId,
                title = "Bachelor of Science in Computer Science",
                organization = "University of California, Berkeley",
                category = CertificateCategories.DEGREE,
                issueDate = "2023-05-20",
                expiryDate = "Lifetime",
                credentialId = "UCB-CS-2023-491",
                credentialUrl = "",
                description = "Graduated with Honors. Specialized in Software Engineering, Distributed Systems, Algorithms & Data Structures, and Computer Vision.",
                skills = listOf("Computer Science", "Algorithms", "Kotlin", "Java", "Python"),
                gradeOrScore = "GPA: 3.88 / 4.00 (Magna Cum Laude)"
            ),
            Certificate(
                userId = userId,
                title = "Deep Learning & Neural Networks Specialization",
                organization = "DeepLearning.AI / Coursera",
                category = CertificateCategories.COURSE,
                issueDate = "2024-02-10",
                expiryDate = "No Expiry",
                credentialId = "DL-AI-90248",
                credentialUrl = "https://coursera.org/verify/specialization/sample",
                description = "Mastered foundational deep learning, convolution neural networks (CNNs), sequence models (RNN/Transformer), and hyperparameter optimization.",
                skills = listOf("Deep Learning", "TensorFlow", "PyTorch", "Computer Vision"),
                gradeOrScore = "Grade: 98.4%"
            ),
            Certificate(
                userId = userId,
                title = "First Place - Global AI Hackathon",
                organization = "TechFest International Innovation League",
                category = CertificateCategories.AWARD,
                issueDate = "2023-11-15",
                expiryDate = "No Expiry",
                credentialId = "TF-HACK-1ST-003",
                credentialUrl = "",
                description = "Awarded 1st place out of 250+ teams for creating an accessible on-device vision assistant for visually impaired users.",
                skills = listOf("AI Innovation", "Product Design", "Team Leadership"),
                gradeOrScore = "1st Place Winner"
            )
        )
    }
}
